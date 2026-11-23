package fr.marstech.mtlinkspray.domain.signature

import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Who created a resource through the web UI (MLS-254 phase B): browser fingerprint hash plus the
 * network side, kept [RETENTION] after [createdAt]. A clue for abuse reports, not a proof: a
 * fingerprint can be spoofed.
 */
data class WriteSignature(
    val id: String = UUID.randomUUID().toString(),
    val resourceType: ResourceType,
    val resourceId: String,
    val fingerprintHash: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: Instant = Instant.now(),
) {
    companion object {
        // 12 months: LCEN retention for hosting providers (MLS-254, to confirm).
        val RETENTION: Duration = Duration.ofDays(365)
    }
}

enum class ResourceType { SHORT_LINK, SPRAY, PASTE }
