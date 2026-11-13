package fr.marstech.mtlinkspray.service

import com.mongodb.client.result.UpdateResult
import fr.marstech.mtlinkspray.dto.ChecklistInstanceRequest
import fr.marstech.mtlinkspray.dto.ChecklistItemRequest
import fr.marstech.mtlinkspray.dto.ChecklistTemplateRequest
import fr.marstech.mtlinkspray.entity.ApiKeyEntity
import fr.marstech.mtlinkspray.entity.ChecklistInstanceEntity
import fr.marstech.mtlinkspray.entity.ChecklistInstanceItem
import fr.marstech.mtlinkspray.entity.ChecklistItemStatus
import fr.marstech.mtlinkspray.entity.ChecklistTemplateEntity
import fr.marstech.mtlinkspray.entity.ChecklistTemplateItem
import fr.marstech.mtlinkspray.repository.ChecklistInstanceRepository
import fr.marstech.mtlinkspray.repository.ChecklistTemplateRepository
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
import org.springframework.data.mongodb.core.index.IndexOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class ChecklistServiceTest {

    private val templateRepository: ChecklistTemplateRepository = mock()
    private val instanceRepository: ChecklistInstanceRepository = mock()
    private val apiKeyService: ApiKeyService = mock()
    private val mongoTemplate: MongoTemplate = mock()
    private lateinit var service: ChecklistService

    private val key = ApiKeyEntity(name = "app", keyId = "k", secretHash = "h", namespace = "ns")
    private val template = ChecklistTemplateEntity(
        id = "tpl-1", namespace = "ns", title = "Trip",
        items = listOf(ChecklistTemplateItem("i1", "Passport"), ChecklistTemplateItem("i2", "Charger", "USB-C")),
        version = 3,
    )
    private val instance = ChecklistInstanceEntity(
        id = "ins-1", namespace = "ns", templateId = "tpl-1", templateVersion = 3, title = "Trip",
        items = listOf(ChecklistInstanceItem("i1", "Passport")),
    )

    @BeforeEach
    fun setUp() {
        service = ChecklistService(templateRepository, instanceRepository, apiKeyService, mongoTemplate, 2, 2, 3)
        whenever(apiKeyService.resolve("key")).thenReturn(key)
        whenever(templateRepository.save(any<ChecklistTemplateEntity>())).thenAnswer { it.arguments[0] }
        whenever(instanceRepository.save(any<ChecklistInstanceEntity>())).thenAnswer { it.arguments[0] }
    }

    private fun request(vararg items: ChecklistItemRequest, version: Long? = null) =
        ChecklistTemplateRequest(title = "Trip", items = items.toList(), version = version)

    @Test
    fun shouldCreateTemplateWithServerItemIds() {
        // When
        val created = service.createTemplate("key", request(ChecklistItemRequest(label = "Passport")))
        // Then
        assertEquals("ns", created.namespace)
        assertEquals(1, created.items.size)
        assertNotNull(created.items[0].itemId)
        assertEquals(null, created.version)
    }

    @Test
    fun shouldRefuseTemplateBeyondNamespaceLimit() {
        whenever(templateRepository.countByNamespace("ns")).thenReturn(2)
        assertThrows<IllegalArgumentException> { service.createTemplate("key", request(ChecklistItemRequest(label = "a"))) }
        verify(templateRepository, never()).save(any<ChecklistTemplateEntity>())
    }

    @Test
    fun shouldRefuseTooManyItems() {
        val items = Array(4) { ChecklistItemRequest(label = "item $it") }
        assertThrows<IllegalArgumentException> { service.createTemplate("key", request(*items)) }
    }

    @Test
    fun shouldRefuseNullItem() {
        @Suppress("UNCHECKED_CAST")
        val items = listOf<ChecklistItemRequest?>(null) as List<ChecklistItemRequest>
        assertThrows<IllegalArgumentException> { service.createTemplate("key", ChecklistTemplateRequest("Trip", items)) }
    }

    @Test
    fun shouldStoreBlankContextAsNull() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        assertEquals(null, service.createInstance("key", ChecklistInstanceRequest("tpl-1", " ")).context)
    }

    @Test
    fun shouldRefuseItemIdOnCreate() {
        assertThrows<IllegalArgumentException> { service.createTemplate("key", request(ChecklistItemRequest("i1", "a"))) }
    }

    @Test
    fun shouldKeepKnownItemIdsAndAddNewOnesOnUpdate() {
        // Given
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        // When
        val updated = service.updateTemplate(
            "key", "tpl-1", request(ChecklistItemRequest("i2", "Charger"), ChecklistItemRequest(label = "Socks"), version = 3),
        )
        // Then
        assertEquals("i2", updated.items[0].itemId)
        assertEquals("Socks", updated.items[1].label)
        assertEquals(3L, updated.version)
    }

    @Test
    fun shouldSaveWithTheVersionTheCallerRead() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        service.updateTemplate("key", "tpl-1", request(ChecklistItemRequest(label = "a"), version = 1))
        val captor = argumentCaptor<ChecklistTemplateEntity>()
        verify(templateRepository).save(captor.capture())
        assertEquals(1L, captor.firstValue.version)
    }

    @Test
    fun shouldRefuseUpdateWithoutVersion() {
        assertThrows<IllegalArgumentException> { service.updateTemplate("key", "tpl-1", request(ChecklistItemRequest(label = "a"))) }
    }

    @Test
    fun shouldRefuseForeignItemIdOnUpdate() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        assertThrows<IllegalArgumentException> {
            service.updateTemplate("key", "tpl-1", request(ChecklistItemRequest("other", "a"), version = 3))
        }
    }

    @Test
    fun shouldRefuseDuplicateItemIds() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        assertThrows<IllegalArgumentException> {
            service.updateTemplate("key", "tpl-1", request(ChecklistItemRequest("i1", "a"), ChecklistItemRequest("i1", "b"), version = 3))
        }
    }

    @Test
    fun shouldAnswerConflictOnStaleVersion() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        whenever(templateRepository.save(any<ChecklistTemplateEntity>())).thenThrow(OptimisticLockingFailureException("stale"))
        val e = assertThrows<ResponseStatusException> {
            service.updateTemplate("key", "tpl-1", request(ChecklistItemRequest(label = "a"), version = 2))
        }
        assertEquals(HttpStatus.CONFLICT, e.statusCode)
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
        assertEquals(listOf(template), service.listTemplates("key", 0, 20))
    }

    @Test
    fun shouldCopyTemplateItemsIntoInstance() {
        // Given
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.of(template))
        // When
        val created = service.createInstance("key", ChecklistInstanceRequest("tpl-1", "Lisbon 2026"))
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
        assertThrows<IllegalArgumentException> { service.createInstance("key", ChecklistInstanceRequest("tpl-1")) }
    }

    @Test
    fun shouldRefuseInstanceOfUnknownTemplate() {
        whenever(templateRepository.findByIdAndNamespace("tpl-1", "ns")).thenReturn(Optional.empty())
        assertThrows<NoSuchElementException> { service.createInstance("key", ChecklistInstanceRequest("tpl-1")) }
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

    @Test
    fun shouldCreateIndexesOffTheStartupThread() {
        val ops: IndexOperations = mock()
        whenever(mongoTemplate.indexOps(any<Class<*>>())).thenReturn(ops)
        service.startIndexCreation()
        verify(ops, org.mockito.kotlin.timeout(2000).times(4)).createIndex(any())
    }

    @Test
    fun shouldCreateIndexesAndSurviveAFailure() {
        val ops: IndexOperations = mock()
        whenever(mongoTemplate.indexOps(any<Class<*>>())).thenReturn(ops)
        service.createIndexes()
        verify(ops, org.mockito.kotlin.times(4)).createIndex(any())

        whenever(mongoTemplate.indexOps(any<Class<*>>())).thenThrow(IllegalStateException("down"))
        service.createIndexes()
    }
}
