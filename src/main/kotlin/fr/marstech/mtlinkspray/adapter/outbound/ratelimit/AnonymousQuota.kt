package fr.marstech.mtlinkspray.adapter.outbound.ratelimit
import fr.marstech.mtlinkspray.domain.shared.QuotaExceededException
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.IP_LITERAL
import fr.marstech.mtlinkspray.domain.shared.QuotaPort
import io.github.bucket4j.Bucket
import java.net.InetAddress
import java.time.Duration
import java.util.concurrent.TimeUnit.NANOSECONDS

/**
 * Per-IP request quota: [quota] tokens per [windowMillis], refilled gradually (token bucket,
 * MLS-161, ADR 0006). Used for anonymous callers of the native-workload APIs (OCR, image
 * compression) and for the public write routes. A window of 0 or less disables the quota.
 */
class AnonymousQuota(
    private val name: String,
    private val quota: Int,
    windowMillis: Long,
    private val hint: String = "retry later or send an X-Api-Key",
) : QuotaPort {

    private val window = Duration.ofMillis(windowMillis)

    // Bounded so a flood of spoofed IPs cannot grow the heap of the 512 MB instance (MLS-238)
    private val buckets: Cache<String, Bucket> = Caffeine.newBuilder()
        .maximumSize(MAX_TRACKED_CLIENTS)
        .expireAfterAccess(window.coerceAtLeast(Duration.ofMillis(1)))
        .build()

    /** Spends [tokens] for [clientIp], or throws a 429 with Retry-After when its bucket holds fewer. */
    override fun count(clientIp: String, tokens: Long) {
        if (window.isZero || window.isNegative) return
        // Never fits, whatever the wait: a 429 would send the client into pointless retries
        if (tokens > quota) throw IllegalArgumentException("$name request of $tokens exceeds the quota of $quota")
        val probe = buckets.get(clientKey(clientIp)) { newBucket() }.tryConsumeAndReturnRemaining(tokens)
        if (!probe.isConsumed) {
            throw QuotaExceededException("$name quota of $quota reached: $hint", NANOSECONDS.toSeconds(probe.nanosToWaitForRefill) + 1)
        }
    }

    private fun newBucket(): Bucket = Bucket.builder()
        .addLimit { it.capacity(quota.toLong()).refillGreedy(quota.toLong(), window) }
        .build()


    companion object {
        private const val MAX_TRACKED_CLIENTS = 10_000L
        private val IP_LITERAL_REGEX = Regex(IP_LITERAL)

        /**
         * One key per IPv4 address, one per IPv6 /64: a single IPv6 subscriber holds a whole /64 and
         * would otherwise get a fresh bucket per address.
         */
        fun clientKey(clientIp: String): String {
            if (!clientIp.contains(':') || !clientIp.matches(IP_LITERAL_REGEX)) return clientIp
            // InetAddress parses as a literal only when the first character is a hex digit or ':', anything
            // else (".:1" matches the regex) would go to a blocking DNS lookup; IPv4-mapped forms come back as IPv4
            if (Character.digit(clientIp[0], 16) == -1 && clientIp[0] != ':') return clientIp
            val bytes = runCatching { InetAddress.getByName(clientIp).address }.getOrNull() ?: return clientIp
            if (bytes.size == 4) return InetAddress.getByAddress(bytes).hostAddress
            return bytes.take(8).joinToString(":", postfix = "::/64") { "%02x".format(it) }
        }
    }
}
