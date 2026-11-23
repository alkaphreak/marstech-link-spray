package fr.marstech.mtlinkspray.adapter.outbound.mongo.signature

import fr.marstech.mtlinkspray.domain.signature.ResourceType
import fr.marstech.mtlinkspray.domain.signature.WriteSignature
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

/** Stored form of [WriteSignature], expired by the TTL index of `MongoWriteSignatureStore`. */
@Document(collection = "mt-link-spray-write-signatures")
@TypeAlias("fr.marstech.mtlinkspray.entity.WriteSignatureEntity")
data class WriteSignatureEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val resourceType: ResourceType,
    val resourceId: String,
    val fingerprintHash: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: Instant = Instant.now(),
) {
    fun toDomain() = WriteSignature(id, resourceType, resourceId, fingerprintHash, ipAddress, userAgent, createdAt)

    companion object {
        fun of(signature: WriteSignature) = with(signature) {
            WriteSignatureEntity(id, resourceType, resourceId, fingerprintHash, ipAddress, userAgent, createdAt)
        }
    }
}
