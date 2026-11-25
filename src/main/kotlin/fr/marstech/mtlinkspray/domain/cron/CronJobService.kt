package fr.marstech.mtlinkspray.domain.cron
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import fr.marstech.mtlinkspray.domain.shared.MailSenderService
import org.slf4j.LoggerFactory
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
 * Cron jobs (MLS-220, ADR 0003): stored through [CronStore], scheduled in memory by [CronSchedulerPort],
 * re-registered at startup ([start]). Single instance only: a second one would run every job twice.
 */
class CronJobService(
    private val store: CronStore,
    private val cron: CronSchedulerPort,
    private val apiKeyResolver: ApiKeyResolver,
    private val mailSenderService: MailSenderService,
    private val cronHttpClient: HttpClient,
    private val maxJobsPerNamespace: Int,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val futures = ConcurrentHashMap<String, ScheduledFuture<*>>()
    internal var resolve: (String) -> Array<InetAddress> = InetAddress::getAllByName

    /** Schedules every enabled job; called once the application is ready. */
    fun start() {
        try {
            store.findEnabledJobs().forEach(::schedule)
            log.info("Cron: {} job(s) scheduled", futures.size)
        } catch (e: RuntimeException) {
            log.error("Cron jobs not scheduled at startup", e)
        }
    }

    /**
     * Stops the scheduler when the service bean is destroyed, before the store it depends on: a running
     * job never meets a closed MongoDB client.
     */
    fun stop() = cron.shutdown()

    fun list(apiKey: String?): List<CronJob> = store.findJobs(namespaceOf(apiKey))

    fun get(apiKey: String?, id: String): CronJob =
        store.findJob(namespaceOf(apiKey), id) ?: throw NoSuchElementException("Cron job $id not found")

    fun create(apiKey: String?, request: CronJobDraft): CronJob {
        val namespace = namespaceOf(apiKey)
        // ponytail: count then save is not atomic, concurrent POSTs can pass the limit by a few
        require(store.countJobs(namespace) < maxJobsPerNamespace) { "Limit of $maxJobsPerNamespace cron jobs reached" }
        validate(request)
        val job = with(request) {
            CronJob(
                namespace = namespace, name = name, schedule = schedule, method = method, url = url, headers = headers,
                body = body, timeoutSeconds = timeoutSeconds, failureThreshold = failureThreshold, isEnabled = isEnabled,
            )
        }
        return store.saveJob(job).also(::reschedule)
    }

    fun update(apiKey: String?, id: String, request: CronJobDraft): CronJob {
        val existing = get(apiKey, id)
        // Same URL: no DNS check now (each run checks it), so a job whose host stopped resolving can still be paused.
        validate(request, checkUrl = request.url != existing.url)
        // A changed target starts a fresh failure count.
        return store.saveJob(request.applyTo(existing).copy(consecutiveFailures = 0)).also(::reschedule)
    }

    fun delete(apiKey: String?, id: String) {
        val job = get(apiKey, id)
        futures.compute(job.id) { _, old -> old?.cancel(false); null }
        store.deleteJob(job.id)
        store.deleteRuns(job.id)
    }

    fun runs(apiKey: String?, id: String, limit: Int): List<CronRun> = store.findRuns(get(apiKey, id).id, limit)

    fun nextRunAt(job: CronJob): Instant? =
        if (job.isEnabled && futures.containsKey(job.id))
            cron.next(job.schedule, LocalDateTime.now(ZoneOffset.UTC))?.toInstant(ZoneOffset.UTC)
        else null

    /**
     * Runs a job once and records the result. Reads the job fresh so an API update made since scheduling applies.
     * Skipped, without a run record, while the namespace's API key is disabled.
     */
    fun execute(jobId: String) {
        val job = store.findJob(jobId)?.takeIf { it.isEnabled } ?: return
        // A disabled or deleted key stops its jobs, and re-enabling it resumes them.
        if (!apiKeyResolver.isNamespaceEnabled(job.namespace)) return
        val startedAt = Instant.now()
        val (httpStatus, error) = call(job)
        val status = if (error == null) CronRunStatus.OK else CronRunStatus.FAILED
        store.saveRun(CronRun(jobId = job.id, startedAt = startedAt, durationMs = Duration.between(startedAt, Instant.now()).toMillis(), status = status, httpStatus = httpStatus, error = error))
        // Atomic: an update resetting the counter during the call is not overwritten by a stale value.
        // Threshold read from the stored job too: a PUT during the call may have changed it.
        val stored = store.recordRunResult(job.id, startedAt, status) ?: return
        if (status == CronRunStatus.FAILED && stored.consecutiveFailures == stored.failureThreshold) alert(stored, stored.consecutiveFailures, error)
    }

    /**
     * One HTTP call, bounded by the job timeout end to end (headers and body), the body itself is
     * never read. Errors are kept generic so the run history does not describe internal hosts.
     */
    private fun call(job: CronJob): Pair<Int?, String?> {
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

    private fun alert(job: CronJob, failures: Int, error: String?) = try {
        mailSenderService.sendMail(
            "[sol4.space cron] ${job.name} failed $failures times",
            "Job ${job.id} (${job.namespace}): ${job.method} ${job.url}\nLast error: $error\nNo new alert until it succeeds again.",
        )
    } catch (e: RuntimeException) {
        log.warn("Cron alert mail not sent for job {}: {}", job.id, e.toString())
    }

    private fun validate(request: CronJobDraft, checkUrl: Boolean = true) {
        CronJobValidator.validateSchedule(request.schedule, cron)
        if (checkUrl) CronJobValidator.validateUrl(request.url, resolve)
        require(request.headers.all { (k, v) -> k.length <= 256 && v.length <= 4096 }) { "Header name or value too long" }
        // HttpClient rejects restricted headers (Host, Connection...) only when building: fail at 400 now, not at every run.
        with(request) { httpRequest(method, url, headers, body, timeoutSeconds) }
    }

    // compute() is atomic per key: concurrent PUTs cannot leave an untracked recurring task behind.
    private fun reschedule(job: CronJob) {
        futures.compute(job.id) { _, old ->
            old?.cancel(false)
            if (job.isEnabled) trigger(job) else null
        }
    }

    private fun schedule(job: CronJob) {
        futures.compute(job.id) { _, old -> old ?: trigger(job) }
    }

    private fun trigger(job: CronJob): ScheduledFuture<*>? = cron.schedule(job.schedule) { execute(job.id) }

    private fun namespaceOf(apiKey: String?) = apiKeyResolver.resolve(apiKey).namespace

    private fun CronJobDraft.applyTo(base: CronJob) = base.copy(
        name = name, schedule = schedule, method = method, url = url, headers = headers, body = body,
        timeoutSeconds = timeoutSeconds, failureThreshold = failureThreshold, isEnabled = isEnabled,
    )

    private fun CronJob.toHttpRequest() = httpRequest(method, url, headers, body, timeoutSeconds)

    private fun httpRequest(method: String, url: String, headers: Map<String, String>, body: String?, timeoutSeconds: Int): HttpRequest =
        HttpRequest.newBuilder(URI(url))
            .timeout(Duration.ofSeconds(timeoutSeconds.toLong()))
            .method(method, body?.let { HttpRequest.BodyPublishers.ofString(it) } ?: HttpRequest.BodyPublishers.noBody())
            .apply { headers.forEach { (k, v) -> header(k, v) } }
            .build()
}
