package fr.marstech.mtlinkspray.adapter.inbound.web.cron
import fr.marstech.mtlinkspray.domain.cron.CronJob
import fr.marstech.mtlinkspray.domain.cron.CronJobDraft
import fr.marstech.mtlinkspray.domain.cron.CronRun
import fr.marstech.mtlinkspray.domain.cron.CronRunStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant

/** Create or replace a cron job. [schedule] is a Spring 6-field cron expression (seconds first). */
data class CronJobRequest(
    // No control characters: the name ends up in the alert mail subject.
    @field:NotBlank @field:Size(max = 100) @field:Pattern(regexp = "[^\\p{Cntrl}]+") val name: String,
    @field:NotBlank @field:Size(max = 100) val schedule: String,
    @field:Pattern(regexp = "GET|HEAD|POST|PUT|PATCH|DELETE") val method: String = "GET",
    @field:NotBlank @field:Size(max = 2048) val url: String,
    @field:Size(max = 20) val headers: Map<String, String> = emptyMap(),
    @field:Size(max = 65536) val body: String? = null,
    @field:Min(1) @field:Max(10) val timeoutSeconds: Int = 10,
    @field:Min(1) @field:Max(100) val failureThreshold: Int = 3,
    val isEnabled: Boolean = true,
) {
    fun toDraft() = CronJobDraft(name, schedule, method, url, headers, body, timeoutSeconds, failureThreshold, isEnabled)
}

data class CronJobDto(
    val id: String,
    val name: String,
    val schedule: String,
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val body: String?,
    val timeoutSeconds: Int,
    val failureThreshold: Int,
    val isEnabled: Boolean,
    val creationDate: Instant,
    val lastRunAt: Instant?,
    val lastStatus: CronRunStatus?,
    val consecutiveFailures: Int,
    val nextRunAt: Instant?,
) {
    companion object {
        fun from(job: CronJob, nextRunAt: Instant?) = CronJobDto(
            job.id, job.name, job.schedule, job.method, job.url, job.headers, job.body,
            job.timeoutSeconds, job.failureThreshold, job.isEnabled, job.creationDate,
            job.lastRunAt, job.lastStatus, job.consecutiveFailures, nextRunAt,
        )
    }
}

data class CronRunDto(
    val startedAt: Instant,
    val durationMs: Long,
    val status: CronRunStatus,
    val httpStatus: Int?,
    val error: String?,
) {
    companion object {
        fun from(run: CronRun) =
            CronRunDto(run.startedAt, run.durationMs, run.status, run.httpStatus, run.error)
    }
}
