package fr.marstech.mtlinkspray.adapter.outbound.http

import fr.marstech.mtlinkspray.domain.cron.WebhookClient
import fr.marstech.mtlinkspray.domain.cron.WebhookRequest
import fr.marstech.mtlinkspray.domain.cron.WebhookResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/** [WebhookClient] over the JDK [HttpClient]. */
@Component
class JdkWebhookClient internal constructor(internal val http: HttpClient) : WebhookClient {

    // Redirects are not followed: a public URL redirecting to a private one would bypass the SSRF check.
    // @Autowired: Spring must never pick the HttpClient constructor, even if an HttpClient bean appears.
    @Autowired
    constructor() : this(
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()
    )

    // HttpClient rejects restricted headers (Host, Connection...) only when building.
    override fun check(request: WebhookRequest) {
        request.toHttpRequest()
    }

    override fun send(request: WebhookRequest): WebhookResult {
        val timeout = request.timeoutSeconds
        val future = try {
            http.sendAsync(request.toHttpRequest(), HttpResponse.BodyHandlers.ofInputStream())
        } catch (e: IllegalArgumentException) {
            return WebhookResult(null, e.message ?: "Rejected URL")
        }
        return try {
            val response = future.get(timeout.toLong(), TimeUnit.SECONDS)
            response.body().close()
            val code = response.statusCode()
            WebhookResult(code, if (code in 200..299) null else "HTTP $code")
        } catch (e: TimeoutException) {
            WebhookResult(null, "Timeout after $timeout s")
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            WebhookResult(null, "Interrupted")
        } catch (e: Exception) {
            WebhookResult(null, if (e.cause is HttpTimeoutException) "Timeout after $timeout s" else "Connection failed")
        } finally {
            future.cancel(true)
        }
    }

    private fun WebhookRequest.toHttpRequest(): HttpRequest =
        HttpRequest.newBuilder(URI(url))
            .timeout(Duration.ofSeconds(timeoutSeconds.toLong()))
            .method(method, body?.let { HttpRequest.BodyPublishers.ofString(it) } ?: HttpRequest.BodyPublishers.noBody())
            .apply { headers.forEach { (k, v) -> header(k, v) } }
            .build()
}
