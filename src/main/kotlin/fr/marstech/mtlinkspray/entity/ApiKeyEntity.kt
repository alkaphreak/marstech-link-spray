package fr.marstech.mtlinkspray.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

/**
 * Represents a client application ("app") authorized to store images through the image storage
 * service (MLS-203). MLS itself is treated as an app like any other.
 *
 * The API key handed out to clients has the form `{keyId}.{secret}`: [keyId] is a public,
 * non-secret identifier used for fast lookup, while only the BCrypt hash of the secret part
 * ([secretHash]) is persisted - the raw secret is never stored.
 */
@Document(collection = "api_keys")
data class ApiKeyEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val name: String,
    @Indexed(unique = true) val keyId: String,
    val secretHash: String,
    @Indexed(unique = true) val namespace: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    var isEnabled: Boolean = true,
)
