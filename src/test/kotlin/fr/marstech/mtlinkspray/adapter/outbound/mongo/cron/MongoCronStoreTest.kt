package fr.marstech.mtlinkspray.adapter.outbound.mongo.cron
import fr.marstech.mtlinkspray.domain.cron.CronJob
import fr.marstech.mtlinkspray.domain.cron.CronRun
import fr.marstech.mtlinkspray.domain.cron.CronRunStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.IndexDefinition
import org.springframework.data.mongodb.core.index.IndexOperations
import java.time.Instant

class MongoCronStoreTest {

    private val mongoTemplate: MongoTemplate = mock()
    private val store = MongoCronStore(mock(), mock(), mongoTemplate, 30)

    @Test
    fun shouldMapJobsAndRunsBothWays() {
        val job = CronJob(
            id = "job-1", namespace = "ns", name = "ping", schedule = "0 */5 * * * *", method = "POST", url = "https://example.com",
            headers = mapOf("X-A" to "1"), body = "{}", timeoutSeconds = 5, failureThreshold = 2, isEnabled = false,
            creationDate = Instant.EPOCH, lastRunAt = Instant.EPOCH, lastStatus = CronRunStatus.FAILED, consecutiveFailures = 4,
        )
        assertEquals(job, CronJobEntity.of(job).toDomain())
        val run = CronRun("run-1", "job-1", Instant.EPOCH, 12, CronRunStatus.FAILED, 503, "HTTP 503")
        assertEquals(run, CronRunEntity.of(run).toDomain())
    }

    @Test
    fun shouldCreateLookupAndTtlIndexes() {
        val runs: IndexOperations = mock()
        val jobs: IndexOperations = mock()
        whenever(mongoTemplate.indexOps(CronRunEntity::class.java)).thenReturn(runs)
        whenever(mongoTemplate.indexOps(CronJobEntity::class.java)).thenReturn(jobs)
        store.createIndexes()
        val created = argumentCaptor<IndexDefinition>().apply { verify(runs, times(2)).createIndex(capture()) }.allValues
        assertEquals(2_592_000L, created[1].indexOptions["expireAfterSeconds"])
        verify(jobs).createIndex(any())
    }

    @Test
    fun shouldStillCreateTtlIndexWhenLookupIndexesFail() {
        val runs: IndexOperations = mock()
        whenever(mongoTemplate.indexOps(CronRunEntity::class.java)).thenReturn(runs)
        whenever(runs.createIndex(any())).thenThrow(IllegalStateException("down")).thenReturn("startedAt_1")
        store.createIndexes()
        verify(runs, times(2)).createIndex(any())
        verify(mongoTemplate, times(0)).indexOps(eq(CronJobEntity::class.java))
    }
}
