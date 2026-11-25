package fr.marstech.mtlinkspray.domain.cron

import java.time.Instant
import java.util.UUID

/** A user-defined cron job (MLS-220), owned by the API key namespace that created it. */
data class CronJob(
    val id: String = UUID.randomUUID().toString(),
    val namespace: String,
    val name: String,
    val schedule: String,
    val method: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val timeoutSeconds: Int,
    val failureThreshold: Int,
    val isEnabled: Boolean = true,
    val creationDate: Instant = Instant.now(),
    val lastRunAt: Instant? = null,
    val lastStatus: CronRunStatus? = null,
    val consecutiveFailures: Int = 0,
)

enum class CronRunStatus { OK, FAILED }

/** One execution of a [CronJob]. */
data class CronRun(
    val id: String = UUID.randomUUID().toString(),
    val jobId: String,
    val startedAt: Instant,
    val durationMs: Long,
    val status: CronRunStatus,
    val httpStatus: Int? = null,
    val error: String? = null,
)

/** A job as the caller sends it. [schedule] is a Spring 6-field cron expression (seconds first). */
data class CronJobDraft(
    val name: String,
    val schedule: String,
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val body: String?,
    val timeoutSeconds: Int,
    val failureThreshold: Int,
    val isEnabled: Boolean,
)
