package fr.marstech.mtlinkspray.adapter.outbound.urlhaus

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import fr.marstech.mtlinkspray.domain.shortener.UrlReputationPort
import org.slf4j.LoggerFactory
import tools.jackson.databind.json.JsonMapper
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Clock
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.text.Charsets.UTF_8

/**
 * [UrlReputationPort] over the URLhaus API (abuse.ch, MLS-120): a URL is malicious when URLhaus lists it
 * (`query_status` `ok`); any other status means not listed. Fail-open: a timeout or an HTTP error lets it through.
 * Only answers are cached; after a failure no call is made for [PAUSE], so an outage costs one timeout per
 * pause instead of one per URL (a spray holds up to 100).
 */
class UrlhausReputation internal constructor(
    private val authKey: String,
    private val http: HttpClient,
    private val clock: Clock,
    private val sprayDeadline: Duration = SPRAY_DEADLINE,
) : UrlReputationPort {

    constructor(authKey: String) : this(
        authKey,
        HttpClient.newBuilder().connectTimeout(TIMEOUT).followRedirects(HttpClient.Redirect.NEVER).build(),
        Clock.systemUTC(),
    )

    private val log = LoggerFactory.getLogger(javaClass)
    private val json = JsonMapper.builder().build()

    @Volatile
    private var pausedUntil = 0L

    // Bounded for the 512 MB instance (MLS-238); a listing can be added or removed, hence the expiry
    private val verdicts: Cache<String, Boolean> = Caffeine.newBuilder()
        .maximumSize(MAX_CACHED_URLS)
        .expireAfterWrite(Duration.ofHours(1))
        .build()

    override fun isMalicious(url: String): Boolean = verdict(url) ?: false

    /**
     * Checks the uncached [urls] in parallel, [MAX_PARALLEL] at a time, under one [SPRAY_DEADLINE]: a spray
     * stays far below App Platform's 30 s request cut (MLS-253). A URL still unanswered at the deadline passes.
     */
    override fun anyMalicious(urls: Collection<String>): Boolean {
        if (urls.any { verdicts.getIfPresent(it) == true }) return true
        val unknown = urls.distinct().filter { verdicts.getIfPresent(it) == null }
        if (unknown.size <= 1) return unknown.any(::isMalicious)
        Executors.newFixedThreadPool(MAX_PARALLEL, Thread.ofVirtual().factory()).use { pool ->
            return pool.invokeAll(unknown.map { Callable { verdict(it) } }, sprayDeadline.toMillis(), MILLISECONDS)
                .any { !it.isCancelled && runCatching { it.get() }.getOrNull() == true }
        }
    }

    private fun verdict(url: String): Boolean? {
        verdicts.getIfPresent(url)?.let { return it }
        if (clock.millis() < pausedUntil) return null
        return query(url)?.also { verdicts.put(url, it) }
    }

    /** No verdict, and no call for [PAUSE]: an outage then costs one timeout per pause, not one per URL. */
    private fun pause(): Boolean? = null.also { pausedUntil = clock.millis() + PAUSE.toMillis() }

    private fun query(url: String): Boolean? {
        val request = HttpRequest.newBuilder(ENDPOINT)
            .timeout(TIMEOUT)
            .header("Auth-Key", authKey)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("url=" + URLEncoder.encode(url, UTF_8)))
            .build()
        val response = try {
            http.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: IOException) {
            log.warn("URLhaus unreachable, links allowed for {}: {}", PAUSE, e.toString())
            return pause()
        } catch (e: InterruptedException) {
            // Our own spray deadline cancelling the call: no pause, or a slow spray would open the gate for all
            Thread.currentThread().interrupt()
            return null
        }
        val code = response.statusCode()
        val status = if (code == 200) {
            runCatching { json.readTree(response.body()).path("query_status").asString() }.getOrNull()
        } else null
        return when {
            status == "ok" -> true
            // A revoked or wrong key disables the check until fixed: louder than an outage
            code == 401 || code == 403 || status == "unknown_auth_key" ->
                pause().also { log.error("URLhaus rejected the Auth-Key (HTTP {}), URL check off: fix URLHAUS_AUTH_KEY", code) }
            // no_results, invalid_url...: not listed. Never a pause, or one odd URL would open the gate for all
            status != null -> false
            else -> pause().also { log.warn("URLhaus answered HTTP {}, links allowed for {}", code, PAUSE) }
        }
    }

    companion object {
        private val ENDPOINT = URI("https://urlhaus-api.abuse.ch/v1/url/")
        private val TIMEOUT = Duration.ofSeconds(3)
        private val SPRAY_DEADLINE = Duration.ofSeconds(8)
        private val PAUSE = Duration.ofMinutes(1)
        private const val MAX_PARALLEL = 10
        private const val MAX_CACHED_URLS = 10_000L
    }
}
