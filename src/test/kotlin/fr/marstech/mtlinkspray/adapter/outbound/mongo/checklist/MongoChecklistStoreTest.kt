package fr.marstech.mtlinkspray.adapter.outbound.mongo.checklist
import fr.marstech.mtlinkspray.domain.checklist.ChecklistInstance
import fr.marstech.mtlinkspray.domain.checklist.ChecklistInstanceItem
import fr.marstech.mtlinkspray.domain.checklist.ChecklistItemStatus
import fr.marstech.mtlinkspray.domain.checklist.ChecklistTemplate
import fr.marstech.mtlinkspray.domain.checklist.ChecklistTemplateItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.IndexOperations
import java.time.Instant

class MongoChecklistStoreTest {

    private val templateRepository: ChecklistTemplateRepository = mock()
    private val instanceRepository: ChecklistInstanceRepository = mock()
    private val mongoTemplate: MongoTemplate = mock()
    private val store = MongoChecklistStore(templateRepository, instanceRepository, mongoTemplate)

    private val template = ChecklistTemplate(
        id = "tpl-1", namespace = "ns", title = "Trip",
        items = listOf(ChecklistTemplateItem("i1", "Passport", "valid")), version = 3, creationDate = Instant.EPOCH,
    )
    private val instance = ChecklistInstance(
        id = "ins-1", namespace = "ns", templateId = "tpl-1", templateVersion = 3, title = "Trip", context = "Lisbon",
        items = listOf(ChecklistInstanceItem("i1", "Passport", "valid", ChecklistItemStatus.SKIPPED)), creationDate = Instant.EPOCH,
    )

    @Test
    fun shouldMapTemplatesBothWays() {
        assertEquals(template, ChecklistTemplateEntity.of(template).toDomain())
        assertEquals("valid", ChecklistTemplateEntity.of(template).items[0].note)
    }

    @Test
    fun shouldMapInstancesBothWays() {
        assertEquals(instance, ChecklistInstanceEntity.of(instance).toDomain())
        assertEquals(ChecklistItemStatus.SKIPPED, ChecklistInstanceEntity.of(instance).items[0].status)
    }

    @Test
    fun shouldAnswerNullOnAStaleReplace() {
        whenever(templateRepository.save(any<ChecklistTemplateEntity>())).thenThrow(OptimisticLockingFailureException("stale"))
        assertNull(store.replaceTemplate(template))
    }

    @Test
    fun shouldCreateIndexesOffTheStartupThread() {
        val ops: IndexOperations = mock()
        whenever(mongoTemplate.indexOps(any<Class<*>>())).thenReturn(ops)
        store.startIndexCreation()
        verify(ops, timeout(2000).times(4)).createIndex(any())
    }

    @Test
    fun shouldCreateIndexesAndSurviveAFailure() {
        val ops: IndexOperations = mock()
        whenever(mongoTemplate.indexOps(any<Class<*>>())).thenReturn(ops)
        store.createIndexes()
        verify(ops, times(4)).createIndex(any())

        whenever(mongoTemplate.indexOps(any<Class<*>>())).thenThrow(IllegalStateException("down"))
        store.createIndexes()
    }
}
