package fr.marstech.mtlinkspray.domain.checklist
import fr.marstech.mtlinkspray.domain.shared.ConflictException
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import com.mongodb.client.result.UpdateResult
import fr.marstech.mtlinkspray.domain.shared.ApiCaller
import fr.marstech.mtlinkspray.adapter.outbound.mongo.checklist.ChecklistInstanceEntity
import fr.marstech.mtlinkspray.adapter.outbound.mongo.checklist.ChecklistInstanceItemEntity
import fr.marstech.mtlinkspray.adapter.outbound.mongo.checklist.ChecklistTemplateEntity
import fr.marstech.mtlinkspray.adapter.outbound.mongo.checklist.ChecklistTemplateItemEntity
import fr.marstech.mtlinkspray.adapter.outbound.mongo.checklist.MongoChecklistStore
import fr.marstech.mtlinkspray.adapter.outbound.mongo.checklist.ChecklistInstanceRepository
import fr.marstech.mtlinkspray.adapter.outbound.mongo.checklist.ChecklistTemplateRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.util.Optional

class ChecklistServiceTest {

    private val templateRepository: ChecklistTemplateRepository = mock()
    private val instanceRepository: ChecklistInstanceRepository = mock()
    private val apiKeyResolver: ApiKeyResolver = mock()
    private val mongoTemplate: MongoTemplate = mock()
    private lateinit var service: ChecklistService

    private val key = ApiCaller(apiKeyId = "key-1", namespace = "ns")
    private val template = ChecklistTemplateEntity(
        id = "tpl-1", namespace = "ns", title = "Trip",
        items = listOf(ChecklistTemplateItemEntity("i1", "Passport"), ChecklistTemplateItemEntity("i2", "Charger", "USB-C")),
        version = 3,
    )
    private val instance = ChecklistInstanceEntity(
        id = "ins-1", namespace = "ns", templateId = "tpl-1", templateVersion = 3, title = "Trip",
        items = listOf(ChecklistInstanceItemEntity("i1", "Passport")),
    )

    @BeforeEach
    fun setUp() {
        service = ChecklistService(MongoChecklistStore(templateRepository, instanceRepository, mongoTemplate), apiKeyResolver, ChecklistLimits(2, 2, 3))
        whenever(apiKeyResolver.resolve("key")).thenReturn(key)
        whenever(templateRepository.save(any<ChecklistTemplateEntity>())).thenAnswer { it.arguments[0] }
        whenever(instanceRepository.save(any<ChecklistInstanceEntity>())).thenAnswer { it.arguments[0] }
    }

    private fun request(vararg items: ChecklistItemDraft, version: Long? = null) =
        ChecklistTemplateDraft(title = "Trip", items = items.toList(), version = version)

    @Test
    fun shouldCreateTemplateWithServerItemIds() {
        // When
        val created = service.createTemplate("key", request(ChecklistItemDraft(label = "Passport")))
        // Then
        assertEquals("ns", created.namespace)
        assertEquals(1, created.items.size)
        assertNotNull(created.items[0].itemId)
        assertEquals(null, created.version)
    }

    @Test
    fun shouldRefuseTemplateBeyondNamespaceLimit() {
        whenever(templateRepository.countByNamespace("ns")).thenReturn(2)
        assertThrows<IllegalArgumentException> { service.createTemplate("key", request(ChecklistItemDraft(label = "a"))) }
        verify(templateRepository, never()).save(any<ChecklistTemplateEntity>())
    }

    @Test
    fun shouldRefuseTooManyItems() {
        val items = Array(4) { ChecklistItemDraft(label = "item $it") }
        assertThrows<IllegalArgumentException> { service.createTemplate("key", request(*items)) }
    }

    @Test
    fun shouldRefuseNullItem() {
        @Suppress("UNCHECKED_CAST")
        val items = listOf<ChecklistItemDraft?>(null) as List<ChecklistItemDraft>
        assertThrows<IllegalArgumentException> { service.createTemplate("key", ChecklistTemplateDraft("Trip", items)) }
    }

    @Test
    fun shouldStoreBlankContextAsNull() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        assertEquals(null, service.createInstance("key", "tpl-1", " ").context)
    }

    @Test
    fun shouldRefuseItemIdOnCreate() {
        assertThrows<IllegalArgumentException> { service.createTemplate("key", request(ChecklistItemDraft("i1", "a"))) }
    }

    @Test
    fun shouldKeepKnownItemIdsAndAddNewOnesOnUpdate() {
        // Given
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        // When
        val updated = service.updateTemplate(
            "key", "tpl-1", request(ChecklistItemDraft("i2", "Charger"), ChecklistItemDraft(label = "Socks"), version = 3),
        )
        // Then
        assertEquals("i2", updated.items[0].itemId)
        assertEquals("Socks", updated.items[1].label)
        assertEquals(3L, updated.version)
    }

    @Test
    fun shouldSaveWithTheVersionTheCallerRead() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        service.updateTemplate("key", "tpl-1", request(ChecklistItemDraft(label = "a"), version = 3))
        val captor = argumentCaptor<ChecklistTemplateEntity>()
        verify(templateRepository).save(captor.capture())
        assertEquals(3L, captor.firstValue.version)
    }

    @Test
    fun shouldAnswerConflictBeforeSavingAStaleVersion() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        val e = assertThrows<ConflictException> {
            service.updateTemplate("key", "tpl-1", request(ChecklistItemDraft(label = "a"), version = 1))
        }
        verify(templateRepository, never()).save(any<ChecklistTemplateEntity>())
    }

    @Test
    fun shouldAnswerConflictWhenAStaleEditNamesARemovedItem() {
        // Given: the caller read version 2, which still had item "gone"; version 3 removed it
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        // When / Then
        val e = assertThrows<ConflictException> {
            service.updateTemplate("key", "tpl-1", request(ChecklistItemDraft("gone", "a"), version = 2))
        }
    }

    @Test
    fun shouldRefuseUpdateWithoutVersion() {
        assertThrows<IllegalArgumentException> { service.updateTemplate("key", "tpl-1", request(ChecklistItemDraft(label = "a"))) }
    }

    @Test
    fun shouldRefuseForeignItemIdOnUpdate() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        assertThrows<IllegalArgumentException> {
            service.updateTemplate("key", "tpl-1", request(ChecklistItemDraft("other", "a"), version = 3))
        }
    }

    @Test
    fun shouldRefuseDuplicateItemIds() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        assertThrows<IllegalArgumentException> {
            service.updateTemplate("key", "tpl-1", request(ChecklistItemDraft("i1", "a"), ChecklistItemDraft("i1", "b"), version = 3))
        }
    }

    @Test
    fun shouldAnswerConflictOnStaleVersion() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        whenever(templateRepository.save(any<ChecklistTemplateEntity>())).thenThrow(OptimisticLockingFailureException("stale"))
        val e = assertThrows<ConflictException> {
            service.updateTemplate("key", "tpl-1", request(ChecklistItemDraft(label = "a"), version = 3))
        }
    }

    @Test
    fun shouldNotFindTemplateOfAnotherNamespace() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.empty())
        assertThrows<NoSuchElementException> { service.getTemplate("key", "tpl-1") }
    }

    @Test
    fun shouldDeleteTemplate() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        service.deleteTemplate("key", "tpl-1")
        verify(templateRepository).deleteById("tpl-1")
    }

    @Test
    fun shouldListTemplatesOfTheNamespace() {
        whenever(templateRepository.findByNamespace(eq("ns"), any<Pageable>())).thenReturn(listOf(template))
        assertEquals(listOf(template.toDomain()), service.listTemplates("key", 0, 20))
    }

    @Test
    fun shouldCopyTemplateItemsIntoInstance() {
        // Given
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        // When
        val created = service.createInstance("key", "tpl-1", "Lisbon 2026")
        // Then
        assertEquals(3L, created.templateVersion)
        assertEquals("Trip", created.title)
        assertEquals(listOf("i1", "i2"), created.items.map { it.itemId })
        assertEquals("USB-C", created.items[1].note)
        assertEquals(ChecklistItemStatus.TODO, created.items[1].status)
    }

    @Test
    fun shouldRefuseInstanceBeyondNamespaceLimit() {
        whenever(instanceRepository.countByNamespace("ns")).thenReturn(2)
        assertThrows<IllegalArgumentException> { service.createInstance("key", "tpl-1", null) }
    }

    @Test
    fun shouldRefuseInstanceOfUnknownTemplate() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.empty())
        assertThrows<NoSuchElementException> { service.createInstance("key", "tpl-1", null) }
    }

    @Test
    fun shouldPickTheListQueryMatchingTheFilters() {
        service.listInstances("key", null, " ", 0, 20)
        service.listInstances("key", "tpl-1", null, 0, 20)
        service.listInstances("key", null, "trip", 0, 20)
        service.listInstances("key", "tpl-1", "trip", 0, 20)
        verify(instanceRepository).findByNamespace(eq("ns"), any())
        verify(instanceRepository).findByNamespaceAndTemplateId(eq("ns"), eq("tpl-1"), any())
        verify(instanceRepository).findByNamespaceAndContext(eq("ns"), eq("trip"), any())
        verify(instanceRepository).findByNamespaceAndTemplateIdAndContext(eq("ns"), eq("tpl-1"), eq("trip"), any())
    }

    @Test
    fun shouldDeleteInstance() {
        whenever(instanceRepository.findByIdAndNamespace("ins-1", "ns")).thenReturn(Optional.of(instance))
        service.deleteInstance("key", "ins-1")
        verify(instanceRepository).deleteById("ins-1")
    }

    @Test
    fun shouldUpdateItemStatusAtomically() {
        // Given
        whenever(mongoTemplate.updateFirst(any<Query>(), any<Update>(), eq(ChecklistInstanceEntity::class.java)))
            .thenReturn(UpdateResult.acknowledged(1, 0, null))
        whenever(instanceRepository.findByIdAndNamespace("ins-1", "ns")).thenReturn(Optional.of(instance))
        // When
        service.updateItemStatus("key", "ins-1", "i1", ChecklistItemStatus.DONE)
        // Then
        val query = argumentCaptor<Query>()
        val update = argumentCaptor<Update>()
        verify(mongoTemplate).updateFirst(query.capture(), update.capture(), eq(ChecklistInstanceEntity::class.java))
        assertEquals("ns", query.firstValue.queryObject["namespace"])
        assertEquals("i1", query.firstValue.queryObject["items.itemId"])
        assertEquals(ChecklistItemStatus.DONE, update.firstValue.updateObject.get("\$set", org.bson.Document::class.java)["items.$[item].status"])
        assertEquals(1, update.firstValue.arrayFilters.size)
    }

    @Test
    fun shouldAnswerNotFoundWhenNoItemMatches() {
        whenever(mongoTemplate.updateFirst(any<Query>(), any<Update>(), eq(ChecklistInstanceEntity::class.java)))
            .thenReturn(UpdateResult.acknowledged(0, 0, null))
        assertThrows<NoSuchElementException> { service.updateItemStatus("key", "ins-1", "nope", ChecklistItemStatus.DONE) }
    }

}
