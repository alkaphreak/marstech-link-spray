package fr.marstech.mtlinkspray.domain.cron

/** One webhook call of a cron job, as stored. */
data class WebhookRequest(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val body: String?,
    val timeoutSeconds: Int,
)

/** [error] is null for a 2xx answer; [httpStatus] is null when no answer came back. */
data class WebhookResult(val httpStatus: Int?, val error: String?)

/** Sends cron webhooks, implemented by `adapter/outbound/http/JdkWebhookClient` (ADR 0007). */
interface WebhookClient {

    /** Fails with [IllegalArgumentException] when the request cannot be built, e.g. a restricted header. */
    fun check(request: WebhookRequest)

    /**
     * One call, bounded by [WebhookRequest.timeoutSeconds] end to end; the body is never read. Errors are
     * kept generic so the run history does not describe internal hosts.
     */
    fun send(request: WebhookRequest): WebhookResult
}
