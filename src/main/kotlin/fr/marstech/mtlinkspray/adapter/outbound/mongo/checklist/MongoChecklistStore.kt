package fr.marstech.mtlinkspray.adapter.outbound.mongo.checklist
import fr.marstech.mtlinkspray.domain.checklist.ChecklistInstance
import fr.marstech.mtlinkspray.domain.checklist.ChecklistItemStatus
import fr.marstech.mtlinkspray.domain.checklist.ChecklistStore
import fr.marstech.mtlinkspray.domain.checklist.ChecklistTemplate
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

private const val NAMESPACE = "namespace"

/** Checklist templates and instances in MongoDB, with their namespace indexes created at startup. */
@Component
class MongoChecklistStore(
    private val templateRepository: ChecklistTemplateRepository,
    private val instanceRepository: ChecklistInstanceRepository,
    private val mongoTemplate: MongoTemplate,
) : ChecklistStore {
    private val log = LoggerFactory.getLogger(javaClass)

    // Off the event thread: without MongoDB (tests, outage) each createIndex waits for the server-selection timeout.
    @EventListener(ApplicationReadyEvent::class)
    fun startIndexCreation() {
        Thread.ofVirtual().name("checklist-indexes").start(::createIndexes)
    }

    internal fun createIndexes() {
        try {
            mongoTemplate.indexOps(ChecklistTemplateEntity::class.java).createIndex(Index().on(NAMESPACE, ASC))
            val instances = mongoTemplate.indexOps(ChecklistInstanceEntity::class.java)
            instances.createIndex(Index().on(NAMESPACE, ASC))
            instances.createIndex(Index().on(NAMESPACE, ASC).on("templateId", ASC))
            instances.createIndex(Index().on(NAMESPACE, ASC).on("context", ASC))
        } catch (e: RuntimeException) {
            log.error("Checklist indexes not created", e)
        }
    }

    override fun findTemplates(namespace: String, page: Int, size: Int) =
        templateRepository.findByNamespace(namespace, pageOf(page, size)).map { it.toDomain() }

    override fun findTemplate(namespace: String, id: String): ChecklistTemplate? =
        templateRepository.findByIdAndNamespace(id, namespace).map { it.toDomain() }.orElse(null)

    override fun countTemplates(namespace: String) = templateRepository.countByNamespace(namespace)

    override fun createTemplate(template: ChecklistTemplate) = templateRepository.save(ChecklistTemplateEntity.of(template)).toDomain()

    override fun replaceTemplate(template: ChecklistTemplate): ChecklistTemplate? = try {
        templateRepository.save(ChecklistTemplateEntity.of(template)).toDomain()
    } catch (_: OptimisticLockingFailureException) {
        null
    }

    // By id: delete(entity) would check the version and fail on a concurrent edit.
    override fun deleteTemplate(id: String) = templateRepository.deleteById(id)

    override fun findInstances(namespace: String, templateId: String?, context: String?, page: Int, size: Int): List<ChecklistInstance> {
        val pageable = pageOf(page, size)
        return when {
            templateId != null && context != null ->
                instanceRepository.findByNamespaceAndTemplateIdAndContext(namespace, templateId, context, pageable)
            templateId != null -> instanceRepository.findByNamespaceAndTemplateId(namespace, templateId, pageable)
            context != null -> instanceRepository.findByNamespaceAndContext(namespace, context, pageable)
            else -> instanceRepository.findByNamespace(namespace, pageable)
        }.map { it.toDomain() }
    }

    override fun findInstance(namespace: String, id: String): ChecklistInstance? =
        instanceRepository.findByIdAndNamespace(id, namespace).map { it.toDomain() }.orElse(null)

    override fun countInstances(namespace: String) = instanceRepository.countByNamespace(namespace)

    override fun createInstance(instance: ChecklistInstance) = instanceRepository.save(ChecklistInstanceEntity.of(instance)).toDomain()

    override fun deleteInstance(id: String) = instanceRepository.deleteById(id)

    override fun setItemStatus(namespace: String, id: String, itemId: String, status: ChecklistItemStatus): Boolean {
        val query = Query(where("_id").`is`(id).and(NAMESPACE).`is`(namespace).and("items.itemId").`is`(itemId))
        val update = Update().set("items.$[item].status", status).filterArray(where("item.itemId").`is`(itemId))
        return mongoTemplate.updateFirst(query, update, ChecklistInstanceEntity::class.java).matchedCount > 0
    }

    private fun pageOf(page: Int, size: Int) = PageRequest.of(page, size, Sort.by("creationDate").descending().and(Sort.by("_id")))
}
