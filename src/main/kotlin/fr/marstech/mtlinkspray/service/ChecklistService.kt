package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.ChecklistInstanceRequest
import fr.marstech.mtlinkspray.dto.ChecklistItemRequest
import fr.marstech.mtlinkspray.dto.ChecklistTemplateRequest
import fr.marstech.mtlinkspray.entity.ChecklistInstanceEntity
import fr.marstech.mtlinkspray.entity.ChecklistInstanceItem
import fr.marstech.mtlinkspray.entity.ChecklistItemStatus
import fr.marstech.mtlinkspray.entity.ChecklistTemplateEntity
import fr.marstech.mtlinkspray.entity.ChecklistTemplateItem
import fr.marstech.mtlinkspray.repository.ChecklistInstanceRepository
import fr.marstech.mtlinkspray.repository.ChecklistTemplateRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

private const val NAMESPACE = "namespace"

/**
 * Checklist templates and instances (MLS-222, ADR 0005), per `X-Api-Key` namespace. An instance
 * copies its template's items at creation; editing or deleting the template never touches it.
 */
@Service
class ChecklistService(
    private val templateRepository: ChecklistTemplateRepository,
    private val instanceRepository: ChecklistInstanceRepository,
    private val apiKeyService: ApiKeyService,
    private val mongoTemplate: MongoTemplate,
    @param:Value($$"${mt.link-spray.checklist.max-templates-per-namespace:50}") private val maxTemplates: Int,
    @param:Value($$"${mt.link-spray.checklist.max-instances-per-namespace:500}") private val maxInstances: Int,
    @param:Value($$"${mt.link-spray.checklist.max-items:200}") private val maxItems: Int,
) {
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

    fun listTemplates(apiKey: String?, page: Int, size: Int): List<ChecklistTemplateEntity> =
        templateRepository.findByNamespace(namespaceOf(apiKey), pageOf(page, size))

    fun getTemplate(apiKey: String?, id: String): ChecklistTemplateEntity = getTemplateIn(namespaceOf(apiKey), id)

    fun createTemplate(apiKey: String?, request: ChecklistTemplateRequest): ChecklistTemplateEntity {
        val namespace = namespaceOf(apiKey)
        // ponytail: count then save is not atomic, concurrent POSTs can pass the limit by a few
        require(templateRepository.countByNamespace(namespace) < maxTemplates) { "Limit of $maxTemplates checklist templates reached" }
        return templateRepository.save(
            ChecklistTemplateEntity(namespace = namespace, title = request.title, items = itemsOf(request, emptySet())),
        )
    }

    fun updateTemplate(apiKey: String?, id: String, request: ChecklistTemplateRequest): ChecklistTemplateEntity {
        val version = requireNotNull(request.version) { "version is required to replace a template" }
        val existing = getTemplateIn(namespaceOf(apiKey), id)
        // Before the item checks: a stale edit naming an item removed since is a conflict, not a bad request.
        if (version != existing.version) throw conflict(id, version)
        val items = itemsOf(request, existing.items.map { it.itemId }.toSet())
        return try {
            // The version read by the caller, not the stored one: a concurrent edit makes save() fail.
            templateRepository.save(existing.copy(title = request.title, items = items, version = version))
        } catch (_: OptimisticLockingFailureException) {
            throw conflict(id, version)
        }
    }

    // By id: delete(entity) would check the version and fail on a concurrent edit.
    fun deleteTemplate(apiKey: String?, id: String) = templateRepository.deleteById(getTemplate(apiKey, id).id)

    fun listInstances(apiKey: String?, templateId: String?, context: String?, page: Int, size: Int): List<ChecklistInstanceEntity> {
        val namespace = namespaceOf(apiKey)
        val pageable = pageOf(page, size)
        val context = context?.takeIf { it.isNotBlank() }
        return when {
            templateId != null && context != null ->
                instanceRepository.findByNamespaceAndTemplateIdAndContext(namespace, templateId, context, pageable)
            templateId != null -> instanceRepository.findByNamespaceAndTemplateId(namespace, templateId, pageable)
            context != null -> instanceRepository.findByNamespaceAndContext(namespace, context, pageable)
            else -> instanceRepository.findByNamespace(namespace, pageable)
        }
    }

    fun getInstance(apiKey: String?, id: String): ChecklistInstanceEntity = getInstanceIn(namespaceOf(apiKey), id)

    fun createInstance(apiKey: String?, request: ChecklistInstanceRequest): ChecklistInstanceEntity {
        val namespace = namespaceOf(apiKey)
        require(instanceRepository.countByNamespace(namespace) < maxInstances) { "Limit of $maxInstances checklist instances reached" }
        val template = getTemplateIn(namespace, request.templateId)
        return instanceRepository.save(
            ChecklistInstanceEntity(
                namespace = namespace, templateId = template.id, templateVersion = template.version ?: 0,
                title = template.title, context = request.context?.takeIf { it.isNotBlank() },
                items = template.items.map { ChecklistInstanceItem(it.itemId, it.label, it.note) },
            ),
        )
    }

    fun deleteInstance(apiKey: String?, id: String) = instanceRepository.deleteById(getInstance(apiKey, id).id)

    /** One atomic update of the matched item: concurrent updates of two items never conflict. */
    fun updateItemStatus(apiKey: String?, id: String, itemId: String, status: ChecklistItemStatus): ChecklistInstanceEntity {
        val namespace = namespaceOf(apiKey)
        val query = Query(where("_id").`is`(id).and(NAMESPACE).`is`(namespace).and("items.itemId").`is`(itemId))
        val update = Update().set("items.$[item].status", status).filterArray(where("item.itemId").`is`(itemId))
        // Matched but unchanged (status already set, a retry) is a success.
        if (mongoTemplate.updateFirst(query, update, ChecklistInstanceEntity::class.java).matchedCount == 0L) {
            throw NoSuchElementException("Checklist item $itemId not found in instance $id")
        }
        return getInstanceIn(namespace, id)
    }

    private fun itemsOf(request: ChecklistTemplateRequest, knownIds: Set<String>): List<ChecklistTemplateItem> {
        // Jackson lets a JSON null through into a non-null Kotlin list.
        require(request.items.none { (it as ChecklistItemRequest?) == null }) { "items must not contain null" }
        require(request.items.size <= maxItems) { "A checklist holds at most $maxItems items" }
        val sentIds = request.items.mapNotNull { it.itemId }
        require(sentIds.size == sentIds.toSet().size) { "Duplicate itemId in items" }
        sentIds.firstOrNull { it !in knownIds }?.let { throw IllegalArgumentException("Unknown itemId $it") }
        return request.items.map { item ->
            item.itemId?.let { ChecklistTemplateItem(it, item.label, item.note) } ?: ChecklistTemplateItem(label = item.label, note = item.note)
        }
    }

    private fun conflict(id: String, version: Long) =
        ResponseStatusException(HttpStatus.CONFLICT, "Template $id was modified or deleted since version $version")

    private fun getTemplateIn(namespace: String, id: String) =
        templateRepository.findByIdAndNamespace(id, namespace).orElseThrow { NoSuchElementException("Checklist template $id not found") }

    private fun getInstanceIn(namespace: String, id: String) =
        instanceRepository.findByIdAndNamespace(id, namespace).orElseThrow { NoSuchElementException("Checklist instance $id not found") }

    private fun pageOf(page: Int, size: Int) = PageRequest.of(page, size, Sort.by("creationDate").descending().and(Sort.by("_id")))

    private fun namespaceOf(apiKey: String?) = apiKeyService.resolve(apiKey).namespace
}
