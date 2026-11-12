package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.CronJobRequest
import fr.marstech.mtlinkspray.entity.CronJobEntity
import fr.marstech.mtlinkspray.entity.CronRunEntity
import fr.marstech.mtlinkspray.entity.CronRunStatus
import fr.marstech.mtlinkspray.repository.ApiKeyRepository
import fr.marstech.mtlinkspray.repository.CronJobRepository
import fr.marstech.mtlinkspray.repository.CronRunRepository
import fr.marstech.mtlinkspray.validation.CronJobValidator
import jakarta.annotation.PreDestroy
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronExpression
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Cron jobs (MLS-220, ADR 0003): stored in MongoDB, scheduled in memory on a private
 * [ThreadPoolTaskScheduler], re-registered at startup. Single instance only: a second one would run
 * every job twice.
 */
@Service
class CronJobService(
    private val jobRepository: CronJobRepository,
    private val runRepository: CronRunRepository,
    private val apiKeyService: ApiKeyService,
    private val apiKeyRepository: ApiKeyRepository,
    private val mailSenderService: MailSenderService,
    private val cronHttpClient: HttpClient,
    private val mongoTemplate: MongoTemplate,
    @param:Value($$"${mt.link-spray.cron.max-jobs-per-namespace:20}") private val maxJobsPerNamespace: Int,
    @param:Value($$"${mt.link-spray.cron.run-retention-days:30}") private val runRetentionDays: Long,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val futures = ConcurrentHashMap<String, ScheduledFuture<*>>()
    internal var resolve: (String) -> Array<InetAddress> = InetAddress::getAllByName

    // Not a bean: an Executor bean would switch off Boot's applicationTaskExecutor.
    // ponytail: one shared pool, each run is capped by its timeout (10 s max); per-namespace limits if namespaces grow
    private val scheduler = ThreadPoolTaskScheduler().apply {
        poolSize = 4
        setThreadNamePrefix("cron-")
        initialize()
    }

    @PreDestroy
    fun stop() = scheduler.shutdown()

    @EventListener(ApplicationReadyEvent::class)
    fun start() {
        // Separate tries: a missing index must not stop the jobs from running.
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
        try {
            jobRepository.findByIsEnabledTrue().forEach(::schedule)
            log.info("Cron: {} job(s) scheduled", futures.size)
        } catch (e: RuntimeException) {
            log.error("Cron jobs not scheduled at startup", e)
        }
    }

    fun list(apiKey: String?): List<CronJobEntity> = jobRepository.findByNamespace(namespaceOf(apiKey))

    fun get(apiKey: String?, id: String): CronJobEntity =
        jobRepository.findByIdAndNamespace(id, namespaceOf(apiKey)).orElseThrow { NoSuchElementException("Cron job $id not found") }

    fun create(apiKey: String?, request: CronJobRequest): CronJobEntity {
        val namespace = namespaceOf(apiKey)
        // ponytail: count then save is not atomic, concurrent POSTs can pass the limit by a few
        require(jobRepository.countByNamespace(namespace) < maxJobsPerNamespace) { "Limit of $maxJobsPerNamespace cron jobs reached" }
        validate(request)
        val job = with(request) {
            CronJobEntity(
                namespace = namespace, name = name, schedule = schedule, method = method, url = url, headers = headers,
                body = body, timeoutSeconds = timeoutSeconds, failureThreshold = failureThreshold, isEnabled = isEnabled,
            )
        }
        return jobRepository.save(job).also(::reschedule)
    }

    fun update(apiKey: String?, id: String, request: CronJobRequest): CronJobEntity {
        val existing = get(apiKey, id)
        // Same URL: no DNS check now (each run checks it), so a job whose host stopped resolving can still be paused.
        validate(request, checkUrl = request.url != existing.url)
        // A changed target starts a fresh failure count.
        return jobRepository.save(request.toEntity(existing).copy(consecutiveFailures = 0)).also(::reschedule)
    }

    fun delete(apiKey: String?, id: String) {
        val job = get(apiKey, id)
        futures.compute(job.id) { _, old -> old?.cancel(false); null }
        jobRepository.deleteById(job.id)
        runRepository.deleteByJobId(job.id)
    }

    fun runs(apiKey: String?, id: String, limit: Int): List<CronRunEntity> =
        runRepository.findByJobIdOrderByStartedAtDesc(get(apiKey, id).id, PageRequest.of(0, limit))

    fun nextRunAt(job: CronJobEntity): Instant? =
        if (job.isEnabled && futures.containsKey(job.id))
            CronExpression.parse(job.schedule).next(LocalDateTime.now(ZoneOffset.UTC))?.toInstant(ZoneOffset.UTC)
        else null

    /**
     * Runs a job once and records the result. Reads the job fresh so an API update made since scheduling applies.
     * Skipped, without a run record, while the namespace's API key is disabled.
     */
    fun execute(jobId: String) {
        val job = jobRepository.findById(jobId).orElse(null)?.takeIf { it.isEnabled } ?: return
        // A disabled or deleted key stops its jobs, and re-enabling it resumes them.
        if (!apiKeyRepository.findByNamespace(job.namespace).map { it.isEnabled }.orElse(false)) return
        val startedAt = Instant.now()
        val (httpStatus, error) = call(job)
        val status = if (error == null) CronRunStatus.OK else CronRunStatus.FAILED
        runRepository.save(CronRunEntity(jobId = job.id, startedAt = startedAt, durationMs = Duration.between(startedAt, Instant.now()).toMillis(), status = status, httpStatus = httpStatus, error = error))
        // Atomic $inc: an update resetting the counter during the call is not overwritten by a stale value.
        val update = Update().set("lastRunAt", startedAt).set("lastStatus", status)
            .let { if (status == CronRunStatus.OK) it.set("consecutiveFailures", 0) else it.inc("consecutiveFailures", 1) }
        // Threshold read from the stored job too: a PUT during the call may have changed it.
        val stored = mongoTemplate.findAndModify(
            Query(where("_id").`is`(job.id)), update, FindAndModifyOptions.options().returnNew(true), CronJobEntity::class.java,
        ) ?: return
        if (status == CronRunStatus.FAILED && stored.consecutiveFailures == stored.failureThreshold) alert(stored, stored.consecutiveFailures, error)
    }

    /**
     * One HTTP call, bounded by the job timeout end to end (headers and body), the body itself is
     * never read. Errors are kept generic so the run history does not describe internal hosts.
     */
    private fun call(job: CronJobEntity): Pair<Int?, String?> {
        val future = try {
            // Again at run time: DNS may have changed since creation.
            // ponytail: this lookup is outside the job timeout, and HttpClient resolves again (DNS rebinding); pin the IP with a custom resolver if the oracle matters
            CronJobValidator.validateUrl(job.url, resolve)
            cronHttpClient.sendAsync(job.toHttpRequest(), HttpResponse.BodyHandlers.ofInputStream())
        } catch (e: IllegalArgumentException) {
            return null to (e.message ?: "Rejected URL")
        }
        return try {
            val response = future.get(job.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            response.body().close()
            val code = response.statusCode()
            code to (if (code in 200..299) null else "HTTP $code")
        } catch (e: TimeoutException) {
            null to "Timeout after ${job.timeoutSeconds} s"
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            null to "Interrupted"
        } catch (e: Exception) {
            null to if (e.cause is HttpTimeoutException) "Timeout after ${job.timeoutSeconds} s" else "Connection failed"
        } finally {
            future.cancel(true)
        }
    }

    private fun alert(job: CronJobEntity, failures: Int, error: String?) = try {
        mailSenderService.sendMail(
            "[sol4.space cron] ${job.name} failed $failures times",
            "Job ${job.id} (${job.namespace}): ${job.method} ${job.url}\nLast error: $error\nNo new alert until it succeeds again.",
        )
    } catch (e: RuntimeException) {
        log.warn("Cron alert mail not sent for job {}: {}", job.id, e.toString())
    }

    private fun validate(request: CronJobRequest, checkUrl: Boolean = true) {
        CronJobValidator.validateSchedule(request.schedule)
        if (checkUrl) CronJobValidator.validateUrl(request.url, resolve)
        require(request.headers.all { (k, v) -> k.length <= 256 && v.length <= 4096 }) { "Header name or value too long" }
        // HttpClient rejects restricted headers (Host, Connection...) only when building: fail at 400 now, not at every run.
        with(request) { httpRequest(method, url, headers, body, timeoutSeconds) }
    }

    // compute() is atomic per key: concurrent PUTs cannot leave an untracked recurring task behind.
    private fun reschedule(job: CronJobEntity) {
        futures.compute(job.id) { _, old ->
            old?.cancel(false)
            if (job.isEnabled) trigger(job) else null
        }
    }

    private fun schedule(job: CronJobEntity) {
        futures.compute(job.id) { _, old -> old ?: trigger(job) }
    }

    private fun trigger(job: CronJobEntity): ScheduledFuture<*>? =
        scheduler.schedule({ execute(job.id) }, CronTrigger(job.schedule, ZoneOffset.UTC))

    private fun namespaceOf(apiKey: String?) = apiKeyService.resolve(apiKey).namespace

    private fun CronJobRequest.toEntity(base: CronJobEntity) = base.copy(
        name = name, schedule = schedule, method = method, url = url, headers = headers, body = body,
        timeoutSeconds = timeoutSeconds, failureThreshold = failureThreshold, isEnabled = isEnabled,
    )

    private fun CronJobEntity.toHttpRequest() = httpRequest(method, url, headers, body, timeoutSeconds)

    private fun httpRequest(method: String, url: String, headers: Map<String, String>, body: String?, timeoutSeconds: Int): HttpRequest =
        HttpRequest.newBuilder(URI(url))
            .timeout(Duration.ofSeconds(timeoutSeconds.toLong()))
            .method(method, body?.let { HttpRequest.BodyPublishers.ofString(it) } ?: HttpRequest.BodyPublishers.noBody())
            .apply { headers.forEach { (k, v) -> header(k, v) } }
            .build()
}
