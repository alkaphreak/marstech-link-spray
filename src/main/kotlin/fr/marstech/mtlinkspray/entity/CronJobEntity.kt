package fr.marstech.mtlinkspray.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

/**
 * A user-defined cron job (MLS-220), owned by the API key namespace that created it.
 * The scheduler writes only the run-state fields, with an atomic update (see CronJobService.execute).
 * Indexes are created at startup by CronJobService (auto-index-creation is off).
 */
@Document(collection = "cron_jobs")
data class CronJobEntity(
    @Id val id: String = UUID.randomUUID().toString(),
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

/** One execution of a [CronJobEntity]; expires after the retention set by the TTL index on [startedAt]. */
@Document(collection = "cron_runs")
data class CronRunEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val jobId: String,
    val startedAt: Instant,
    val durationMs: Long,
    val status: CronRunStatus,
    val httpStatus: Int? = null,
    val error: String? = null,
)
