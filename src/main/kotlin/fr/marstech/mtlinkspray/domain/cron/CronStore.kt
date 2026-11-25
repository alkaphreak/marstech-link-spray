package fr.marstech.mtlinkspray.domain.cron

import java.time.Instant

/** Cron jobs and their run history. */
interface CronStore {
    fun findEnabledJobs(): List<CronJob>
    fun findJobs(namespace: String): List<CronJob>
    fun findJob(namespace: String, id: String): CronJob?
    fun findJob(id: String): CronJob?
    fun countJobs(namespace: String): Long
    fun saveJob(job: CronJob): CronJob
    fun deleteJob(id: String)

    /**
     * One atomic write of the run state: last run and status, failures reset on [CronRunStatus.OK] or
     * incremented on [CronRunStatus.FAILED]. Returns the job as stored after the write, null when it is gone.
     */
    fun recordRunResult(jobId: String, startedAt: Instant, status: CronRunStatus): CronJob?

    fun saveRun(run: CronRun)

    /** Newest first. */
    fun findRuns(jobId: String, limit: Int): List<CronRun>
    fun deleteRuns(jobId: String)
}
