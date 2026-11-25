package fr.marstech.mtlinkspray.adapter.outbound.ratelimit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import fr.marstech.mtlinkspray.domain.shared.QuotaExceededException

class AnonymousQuotaTest {

    @Test
    fun shouldRejectWithRetryAfterWhenQuotaIsSpent() {
        // Given
        val quota = AnonymousQuota("Write", 2, 600_000, "retry later")
        repeat(2) { quota.count("203.0.113.7") }

        // When
        val e = assertThrows<QuotaExceededException> { quota.count("203.0.113.7") }

        // Then: one token refills every 300 s
        assertEquals("Write quota of 2 reached: retry later", e.message)
        val retryAfter = e.retryAfterSeconds
        assertTrue(retryAfter in 1..301, "Retry-After was $retryAfter")
    }

    @Test
    fun shouldCountEachIpSeparately() {
        // Given
        val quota = AnonymousQuota("Write", 1, 600_000)
        quota.count("203.0.113.7")

        // When / Then: another IP still has its own token
        quota.count("198.51.100.9")
        assertThrows<QuotaExceededException> { quota.count("203.0.113.7") }
    }

    @Test
    fun shouldNotLimitWhenWindowIsZero() {
        // Given
        val quota = AnonymousQuota("Write", 1, 0)

        // When / Then
        repeat(5) { quota.count("203.0.113.7") }
    }

    @Test
    fun shouldShareOneBucketPerIpv6Slash64() {
        // Given
        val quota = AnonymousQuota("Write", 1, 600_000)
        quota.count("2001:db8:1:2::1")

        // When / Then: another address of the same /64 hits the same bucket
        assertThrows<QuotaExceededException> { quota.count("2001:db8:1:2:ffff::9") }
        quota.count("2001:db8:1:3::1")
    }

    @Test
    fun shouldKeyIpv4AndNonLiteralsAsIs() {
        assertEquals("203.0.113.7", AnonymousQuota.clientKey("203.0.113.7"))
        assertEquals("203.0.113.7", AnonymousQuota.clientKey("::ffff:203.0.113.7"))
        assertEquals("unknown", AnonymousQuota.clientKey("unknown"))
        assertEquals("20:01:0d:b8:00:01:00:02::/64", AnonymousQuota.clientKey("2001:db8:1:2::1"))
    }

    @Test
    fun shouldRejectAsBadRequestWhenRequestExceedsCapacity() {
        // Given
        val quota = AnonymousQuota("Write", 2, 60_000)

        // When
        val e = assertThrows<IllegalArgumentException> { quota.count("203.0.113.7", 5) }

        // Then: no Retry-After, waiting would never help
    }

    @Test
    fun shouldNotParseValuesThatInetAddressWouldResolve() {
        assertEquals(".:1", AnonymousQuota.clientKey(".:1"))
    }
}
