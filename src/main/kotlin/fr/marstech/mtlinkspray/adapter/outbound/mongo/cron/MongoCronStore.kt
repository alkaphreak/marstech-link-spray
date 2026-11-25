package fr.marstech.mtlinkspray.adapter.outbound.mongo.cron
import fr.marstech.mtlinkspray.domain.cron.CronJob
import fr.marstech.mtlinkspray.domain.cron.CronRun
import fr.marstech.mtlinkspray.domain.cron.CronRunStatus
import fr.marstech.mtlinkspray.domain.cron.CronStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/** Cron jobs and runs in MongoDB, with the lookup indexes and the `cron_runs` TTL index created at startup. */
@Component
class MongoCronStore(
    private val jobRepository: CronJobRepository,
    private val runRepository: CronRunRepository,
    private val mongoTemplate: MongoTemplate,
    @param:Value($$"${mt.link-spray.cron.run-retention-days:30}") private val runRetentionDays: Long,
) : CronStore {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun createIndexes() {
        // Separate tries: a missing lookup index must not stop the TTL one.
        // ponytail: no retry, and a changed retention conflicts with the existing TTL index (drop it by hand)
        try {
            mongoTemplate.indexOps(CronRunEntity::class.java).createIndex(Index().on("jobId", ASC).on("startedAt", DESC))
            mongoTemplate.indexOps(CronJobEntity::class.java).createIndex(Index().on("namespace", ASC))
        } catch (e: RuntimeException) {
            log.error("Cron lookup indexes not created", e)
        }
        try {
            mongoTemplate.indexOps(CronRunEntity::class.java)
                .createIndex(Index().on("startedAt", ASC).expire(Duration.ofDays(runRetentionDays)))
        } catch (e: RuntimeException) {
            log.error("Cron run TTL index not created: runs will not expire", e)
        }
    }

    override fun findEnabledJobs() = jobRepository.findByIsEnabledTrue().map { it.toDomain() }

    override fun findJobs(namespace: String) = jobRepository.findByNamespace(namespace).map { it.toDomain() }

    override fun findJob(namespace: String, id: String): CronJob? =
        jobRepository.findByIdAndNamespace(id, namespace).map { it.toDomain() }.orElse(null)

    override fun findJob(id: String): CronJob? = jobRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun countJobs(namespace: String) = jobRepository.countByNamespace(namespace)

    override fun saveJob(job: CronJob) = jobRepository.save(CronJobEntity.of(job)).toDomain()

    override fun deleteJob(id: String) = jobRepository.deleteById(id)

    // $inc, not a read-modify-write: an update resetting the counter during the call is not overwritten.
    override fun recordRunResult(jobId: String, startedAt: Instant, status: CronRunStatus): CronJob? {
        val update = Update().set("lastRunAt", startedAt).set("lastStatus", status)
            .let { if (status == CronRunStatus.OK) it.set("consecutiveFailures", 0) else it.inc("consecutiveFailures", 1) }
        return mongoTemplate.findAndModify(
            Query(where("_id").`is`(jobId)), update, FindAndModifyOptions.options().returnNew(true), CronJobEntity::class.java,
        )?.toDomain()
    }

    override fun saveRun(run: CronRun) {
        runRepository.save(CronRunEntity.of(run))
    }

    override fun findRuns(jobId: String, limit: Int) =
        runRepository.findByJobIdOrderByStartedAtDesc(jobId, PageRequest.of(0, limit)).map { it.toDomain() }

    override fun deleteRuns(jobId: String) = runRepository.deleteByJobId(jobId)
}
