package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.conf.ValidationConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.entity.CronJobEntity
import fr.marstech.mtlinkspray.service.CronJobService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [CronJobApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class, ValidationConfig::class)
class CronJobApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var cronJobService: CronJobService

    private val job = CronJobEntity(
        id = "job-1", namespace = "ns", name = "ping", schedule = "0 */5 * * * *", method = "GET",
        url = "https://example.com/health", timeoutSeconds = 5, failureThreshold = 3,
    )

    private val validBody = """{"name":"ping","schedule":"0 */5 * * * *","url":"https://example.com/health"}"""

    @Test
    fun shouldCreateJob() {
        // Given
        whenever(cronJobService.create(eq("key"), any())).thenReturn(job)
        // When / Then
        mockMvc.perform(post("/api/cron/jobs").header("X-Api-Key", "key").contentType(MediaType.APPLICATION_JSON).content(validBody))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value("job-1"))
            .andExpect(jsonPath("$.namespace").doesNotExist())
    }

    @Test
    fun shouldRejectUnknownMethod() {
        mockMvc.perform(
            post("/api/cron/jobs").header("X-Api-Key", "key").contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"ping","schedule":"0 */5 * * * *","url":"https://example.com","method":"TRACE"}"""),
        ).andExpect(status().isBadRequest)
        verify(cronJobService, never()).create(any(), any())
    }

    @Test
    fun shouldReturnBadRequestWhenServiceRejectsSchedule() {
        whenever(cronJobService.create(eq("key"), any())).thenThrow(IllegalArgumentException("too frequent"))
        mockMvc.perform(post("/api/cron/jobs").header("X-Api-Key", "key").contentType(MediaType.APPLICATION_JSON).content(validBody))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnUnauthorizedWithoutApiKey() {
        whenever(cronJobService.list(null)).thenAnswer { throw IllegalAccessException("Missing API key") }
        mockMvc.perform(get("/api/cron/jobs")).andExpect(status().isUnauthorized)
    }

    @Test
    fun shouldReturnNotFoundForUnknownJob() {
        whenever(cronJobService.get("key", "nope")).thenThrow(NoSuchElementException("Cron job nope not found"))
        mockMvc.perform(get("/api/cron/jobs/nope").header("X-Api-Key", "key")).andExpect(status().isNotFound)
    }

    @Test
    fun shouldListRuns() {
        whenever(cronJobService.runs("key", "job-1", 50)).thenReturn(emptyList())
        mockMvc.perform(get("/api/cron/jobs/job-1/runs").header("X-Api-Key", "key"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    fun shouldRejectRunsLimitAboveMax() {
        mockMvc.perform(get("/api/cron/jobs/job-1/runs").param("limit", "501").header("X-Api-Key", "key"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldListJobsWithNextRun() {
        whenever(cronJobService.list("key")).thenReturn(listOf(job))
        whenever(cronJobService.nextRunAt(job)).thenReturn(java.time.Instant.parse("2026-11-12T00:05:00Z"))
        mockMvc.perform(get("/api/cron/jobs").header("X-Api-Key", "key"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].nextRunAt").value("2026-11-12T00:05:00Z"))
    }

    @Test
    fun shouldUpdateJob() {
        whenever(cronJobService.update(eq("key"), eq("job-1"), any())).thenReturn(job)
        mockMvc.perform(put("/api/cron/jobs/job-1").header("X-Api-Key", "key").contentType(MediaType.APPLICATION_JSON).content(validBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("ping"))
    }

    @Test
    fun shouldRejectNameWithLineBreak() {
        mockMvc.perform(
            post("/api/cron/jobs").header("X-Api-Key", "key").contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"ping\r\nBcc: x@y.z","schedule":"0 */5 * * * *","url":"https://example.com"}"""),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun shouldDeleteJob() {
        mockMvc.perform(delete("/api/cron/jobs/job-1").header("X-Api-Key", "key")).andExpect(status().isNoContent)
        verify(cronJobService).delete("key", "job-1")
    }
}
