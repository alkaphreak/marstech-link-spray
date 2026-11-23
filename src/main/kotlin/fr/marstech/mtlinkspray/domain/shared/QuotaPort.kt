package fr.marstech.mtlinkspray.domain.shared

/** Per-client request quota (ADR 0006), implemented by the Bucket4j limiter in `adapter/outbound/ratelimit`. */
interface QuotaPort {
    /**
     * Spends [tokens] for [clientIp]. Throws a 429 when its budget holds fewer, and a 400 when [tokens]
     * exceeds the whole quota (it would never fit).
     */
    fun count(clientIp: String, tokens: Long = 1)
}
