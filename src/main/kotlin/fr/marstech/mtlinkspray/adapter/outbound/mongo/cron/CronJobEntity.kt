package fr.marstech.mtlinkspray.adapter.outbound.mongo.cron

import fr.marstech.mtlinkspray.domain.cron.CronJob
import fr.marstech.mtlinkspray.domain.cron.CronRun
import fr.marstech.mtlinkspray.domain.cron.CronRunStatus
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

/**
 * A user-defined cron job (MLS-220), owned by the API key namespace that created it.
 * The scheduler writes only the run-state fields, with an atomic update (MongoCronStore.recordRunResult).
 * Indexes are created at startup by MongoCronStore (auto-index-creation is off).
 */
@Document(collection = "cron_jobs")
@TypeAlias("fr.marstech.mtlinkspray.entity.CronJobEntity")
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
) {
    fun toDomain() = CronJob(
        id, namespace, name, schedule, method, url, headers, body, timeoutSeconds, failureThreshold, isEnabled,
        creationDate, lastRunAt, lastStatus, consecutiveFailures,
    )

    companion object {
        fun of(j: CronJob) = CronJobEntity(
            j.id, j.namespace, j.name, j.schedule, j.method, j.url, j.headers, j.body, j.timeoutSeconds, j.failureThreshold,
            j.isEnabled, j.creationDate, j.lastRunAt, j.lastStatus, j.consecutiveFailures,
        )
    }
}

/** One execution of a [CronJobEntity]; expires after the retention set by the TTL index on [startedAt]. */
@Document(collection = "cron_runs")
@TypeAlias("fr.marstech.mtlinkspray.entity.CronRunEntity")
data class CronRunEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val jobId: String,
    val startedAt: Instant,
    val durationMs: Long,
    val status: CronRunStatus,
    val httpStatus: Int? = null,
    val error: String? = null,
) {
    fun toDomain() = CronRun(id, jobId, startedAt, durationMs, status, httpStatus, error)

    companion object {
        fun of(r: CronRun) = CronRunEntity(r.id, r.jobId, r.startedAt, r.durationMs, r.status, r.httpStatus, r.error)
    }
}
