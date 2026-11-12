package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.CronJobRequest
import fr.marstech.mtlinkspray.entity.ApiKeyEntity
import fr.marstech.mtlinkspray.entity.CronJobEntity
import fr.marstech.mtlinkspray.entity.CronRunEntity
import fr.marstech.mtlinkspray.entity.CronRunStatus
import fr.marstech.mtlinkspray.repository.ApiKeyRepository
import fr.marstech.mtlinkspray.repository.CronJobRepository
import fr.marstech.mtlinkspray.repository.CronRunRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
import org.springframework.data.mongodb.core.MongoTemplate
import org.bson.Document
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.net.InetAddress
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

class CronJobServiceTest {

    private val jobRepository: CronJobRepository = mock()
    private val runRepository: CronRunRepository = mock()
    private val apiKeyService: ApiKeyService = mock()
    private val apiKeyRepository: ApiKeyRepository = mock()
    private val mailSenderService: MailSenderService = mock()
    private val httpClient: HttpClient = mock()
    private val mongoTemplate: MongoTemplate = mock()
    private lateinit var service: CronJobService

    private val job = CronJobEntity(
        id = "job-1", namespace = "ns", name = "ping", schedule = "0 */5 * * * *", method = "GET",
        url = "https://example.com/health", timeoutSeconds = 1, failureThreshold = 3,
    )

    private val key = ApiKeyEntity(name = "app", keyId = "k", secretHash = "h", namespace = "ns")

    @BeforeEach
    fun setUp() {
        service = CronJobService(jobRepository, runRepository, apiKeyService, apiKeyRepository, mailSenderService, httpClient, mongoTemplate, 2, 30)
        service.resolve = { arrayOf(InetAddress.getByName("93.184.215.14")) }
        whenever(apiKeyService.resolve("key")).thenReturn(key)
        whenever(apiKeyRepository.findByNamespace("ns")).thenReturn(Optional.of(key))
        whenever(jobRepository.save(any<CronJobEntity>())).thenAnswer { it.arguments[0] }
    }

    @AfterEach
    fun tearDown() = service.stop()

    private fun givenResponse(status: Int) {
        val response: HttpResponse<InputStream> = mock()
        whenever(response.statusCode()).thenReturn(status)
        whenever(response.body()).thenReturn(InputStream.nullInputStream())
        whenever(httpClient.sendAsync(any<HttpRequest>(), any<HttpResponse.BodyHandler<InputStream>>()))
            .thenReturn(CompletableFuture.completedFuture(response))
    }

    // The atomic update returns the counter as stored after the run.
    private fun givenStoredJob(failuresAfterRun: Int) {
        whenever(jobRepository.findById("job-1")).thenReturn(Optional.of(job))
        whenever(mongoTemplate.findAndModify(any<Query>(), any<Update>(), any<FindAndModifyOptions>(), eq(CronJobEntity::class.java)))
            .thenReturn(job.copy(consecutiveFailures = failuresAfterRun))
    }

    private fun storedUpdate(): Document =
        argumentCaptor<Update>().apply { verify(mongoTemplate).findAndModify(any<Query>(), capture(), any<FindAndModifyOptions>(), eq(CronJobEntity::class.java)) }
            .firstValue.updateObject

    @Test
    fun shouldResetFailuresWhenRunSucceeds() {
        // Given
        givenStoredJob(failuresAfterRun = 0)
        givenResponse(204)
        // When
        service.execute("job-1")
        // Then
        assertEquals(0, (storedUpdate()["\$set"] as Document)["consecutiveFailures"])
        verify(mailSenderService, never()).sendMail(any(), any())
    }

    @Test
    fun shouldSendOneAlertWhenFailuresReachThreshold() {
        // Given
        givenStoredJob(failuresAfterRun = 3)
        givenResponse(503)
        // When
        service.execute("job-1")
        // Then
        val run = argumentCaptor<CronRunEntity>().apply { verify(runRepository).save(capture()) }.firstValue
        assertEquals(CronRunStatus.FAILED, run.status)
        assertEquals(503, run.httpStatus)
        assertEquals(1, (storedUpdate()["\$inc"] as Document)["consecutiveFailures"])
        verify(mailSenderService).sendMail(any(), any())
    }

    @Test
    fun shouldAlertOnStoredThresholdWhenChangedDuringRun() {
        // Given: the job had threshold 3 when the run started, a PUT lowered it to 1
        whenever(jobRepository.findById("job-1")).thenReturn(Optional.of(job))
        whenever(mongoTemplate.findAndModify(any<Query>(), any<Update>(), any<FindAndModifyOptions>(), eq(CronJobEntity::class.java)))
            .thenReturn(job.copy(consecutiveFailures = 1, failureThreshold = 1))
        givenResponse(500)
        // When
        service.execute("job-1")
        // Then
        verify(mailSenderService).sendMail(any(), any())
    }

    @Test
    fun shouldSkipRunWhenJobDisabledSinceScheduling() {
        // Given
        whenever(jobRepository.findById("job-1")).thenReturn(Optional.of(job.copy(isEnabled = false)))
        // When
        service.execute("job-1")
        // Then
        verify(httpClient, never()).sendAsync(any<HttpRequest>(), any<HttpResponse.BodyHandler<InputStream>>())
        verify(runRepository, never()).save(any<CronRunEntity>())
    }

    @Test
    fun shouldNotAlertAgainPastThreshold() {
        // Given
        givenStoredJob(failuresAfterRun = 4)
        givenResponse(500)
        // When
        service.execute("job-1")
        // Then
        verify(mailSenderService, never()).sendMail(any(), any())
    }

    @Test
    fun shouldKeepRecordingWhenAlertMailFails() {
        // Given
        givenStoredJob(failuresAfterRun = 3)
        givenResponse(500)
        whenever(mailSenderService.sendMail(any(), any())).thenThrow(RuntimeException("smtp down"))
        // When
        service.execute("job-1")
        // Then
        verify(runRepository).save(any<CronRunEntity>())
    }

    @Test
    fun shouldFailRunWithoutCallingWhenHostNowResolvesPrivate() {
        // Given
        givenStoredJob(failuresAfterRun = 1)
        service.resolve = { arrayOf(InetAddress.getByName("10.0.0.5")) }
        // When
        service.execute("job-1")
        // Then
        verify(httpClient, never()).sendAsync(any<HttpRequest>(), any<HttpResponse.BodyHandler<InputStream>>())
        assertEquals(1, (storedUpdate()["\$inc"] as Document)["consecutiveFailures"])
    }

    @Test
    fun shouldRecordTimeoutWhenResponseNeverComes() {
        // Given
        givenStoredJob(failuresAfterRun = 1)
        whenever(httpClient.sendAsync(any<HttpRequest>(), any<HttpResponse.BodyHandler<InputStream>>())).thenReturn(CompletableFuture())
        // When
        service.execute("job-1")
        // Then
        val run = argumentCaptor<CronRunEntity>().apply { verify(runRepository).save(capture()) }.firstValue
        assertEquals("Timeout after 1 s", run.error)
    }

    @Test
    fun shouldRecordGenericErrorWhenConnectionFails() {
        // Given
        givenStoredJob(failuresAfterRun = 1)
        whenever(httpClient.sendAsync(any<HttpRequest>(), any<HttpResponse.BodyHandler<InputStream>>()))
            .thenReturn(CompletableFuture.failedFuture(java.net.ConnectException("Connection refused: 10.0.0.1:22")))
        // When
        service.execute("job-1")
        // Then
        val run = argumentCaptor<CronRunEntity>().apply { verify(runRepository).save(capture()) }.firstValue
        assertEquals("Connection failed", run.error)
    }

    @Test
    fun shouldStillScheduleJobsWhenIndexCreationFails() {
        // Given
        whenever(mongoTemplate.indexOps(any<Class<*>>())).thenThrow(RuntimeException("mongo down"))
        whenever(jobRepository.findByIsEnabledTrue()).thenReturn(listOf(job))
        // When
        service.start()
        // Then
        assertNotNull(service.nextRunAt(job))
    }

    @Test
    fun shouldSurviveJobLoadingFailureAtStartup() {
        whenever(jobRepository.findByIsEnabledTrue()).thenThrow(RuntimeException("mongo down"))
        service.start()
        assertNull(service.nextRunAt(job))
    }

    @Test
    fun shouldRecordTimeoutWhenClientTimesOut() {
        givenStoredJob(failuresAfterRun = 1)
        whenever(httpClient.sendAsync(any<HttpRequest>(), any<HttpResponse.BodyHandler<InputStream>>()))
            .thenReturn(CompletableFuture.failedFuture(java.net.http.HttpTimeoutException("request timed out")))
        service.execute("job-1")
        val run = argumentCaptor<CronRunEntity>().apply { verify(runRepository).save(capture()) }.firstValue
        assertEquals("Timeout after 1 s", run.error)
    }

    @Test
    fun shouldNotAlertWhenJobDeletedDuringRun() {
        // Given: found before the call, gone when the run state is written
        whenever(jobRepository.findById("job-1")).thenReturn(Optional.of(job.copy(failureThreshold = 1)))
        givenResponse(500)
        // When
        service.execute("job-1")
        // Then
        verify(mailSenderService, never()).sendMail(any(), any())
    }

    @Test
    fun shouldSkipRunWhenApiKeyDisabled() {
        // Given
        givenStoredJob(failuresAfterRun = 0)
        whenever(apiKeyRepository.findByNamespace("ns")).thenReturn(Optional.of(key.copy(isEnabled = false)))
        // When
        service.execute("job-1")
        // Then
        verify(httpClient, never()).sendAsync(any<HttpRequest>(), any<HttpResponse.BodyHandler<InputStream>>())
        verify(runRepository, never()).save(any<CronRunEntity>())
    }

    @Test
    fun shouldIgnoreRunOfDeletedJob() {
        whenever(jobRepository.findById("job-1")).thenReturn(Optional.empty())
        service.execute("job-1")
        verify(runRepository, never()).save(any<CronRunEntity>())
    }

    @Test
    fun shouldCreateAndScheduleJobInCallerNamespace() {
        // Given
        whenever(jobRepository.countByNamespace("ns")).thenReturn(0)
        // When
        val created = service.create("key", CronJobRequest(name = "ping", schedule = "0 */5 * * * *", url = "https://example.com"))
        // Then
        assertEquals("ns", created.namespace)
        assertNotNull(service.nextRunAt(created))
    }

    @Test
    fun shouldNotScheduleDisabledJob() {
        whenever(jobRepository.countByNamespace("ns")).thenReturn(0)
        val created = service.create("key", CronJobRequest(name = "ping", schedule = "0 */5 * * * *", url = "https://example.com", isEnabled = false))
        assertNull(service.nextRunAt(created))
    }

    @Test
    fun shouldRejectCreationPastNamespaceLimit() {
        whenever(jobRepository.countByNamespace("ns")).thenReturn(2)
        assertThrows<IllegalArgumentException> {
            service.create("key", CronJobRequest(name = "ping", schedule = "0 */5 * * * *", url = "https://example.com"))
        }
    }

    @Test
    fun shouldRejectRestrictedHeaderAtCreation() {
        whenever(jobRepository.countByNamespace("ns")).thenReturn(0)
        assertThrows<IllegalArgumentException> {
            service.create("key", CronJobRequest(name = "ping", schedule = "0 */5 * * * *", url = "https://example.com", headers = mapOf("Host" to "internal")))
        }
    }

    @Test
    fun shouldResetFailuresAndUnscheduleOnUpdate() {
        // Given
        whenever(jobRepository.findByIdAndNamespace("job-1", "ns")).thenReturn(Optional.of(job.copy(consecutiveFailures = 5)))
        // When
        val updated = service.update("key", "job-1", CronJobRequest(name = "ping", schedule = "0 0 * * * *", url = "https://example.com", isEnabled = false))
        // Then
        assertEquals(0, updated.consecutiveFailures)
        assertEquals("0 0 * * * *", updated.schedule)
        assertNull(service.nextRunAt(updated))
    }

    @Test
    fun shouldDeleteJobAndItsRuns() {
        whenever(jobRepository.findByIdAndNamespace("job-1", "ns")).thenReturn(Optional.of(job))
        service.delete("key", "job-1")
        verify(jobRepository).deleteById("job-1")
        verify(runRepository).deleteByJobId("job-1")
    }

    @Test
    fun shouldHideJobOfAnotherNamespace() {
        whenever(jobRepository.findByIdAndNamespace("job-1", "ns")).thenReturn(Optional.empty())
        assertThrows<NoSuchElementException> { service.get("key", "job-1") }
    }

    @Test
    fun shouldScheduleEnabledJobsAtStartup() {
        whenever(mongoTemplate.indexOps(any<Class<*>>())).thenReturn(mock())
        whenever(jobRepository.findByIsEnabledTrue()).thenReturn(listOf(job))
        service.start()
        assertNotNull(service.nextRunAt(job))
    }
}
