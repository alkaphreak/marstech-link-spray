package fr.marstech.mtlinkspray.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

/**
 * Who created a resource through the web UI (MLS-254 phase B): browser fingerprint hash plus the
 * network side, kept 12 months by a TTL index on [createdAt] (see `WriteSignatureService`).
 * A clue for abuse reports, not a proof: a fingerprint can be spoofed.
 */
@Document(collection = "mt-link-spray-write-signatures")
data class WriteSignatureEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val resourceType: ResourceType,
    val resourceId: String,
    val fingerprintHash: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: Instant = Instant.now(),
) {
    enum class ResourceType { SHORT_LINK, SPRAY, PASTE }
}
