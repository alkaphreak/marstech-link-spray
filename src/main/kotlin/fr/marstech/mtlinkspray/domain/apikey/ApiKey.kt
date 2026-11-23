package fr.marstech.mtlinkspray.domain.apikey

import java.time.LocalDateTime
import java.util.UUID

/**
 * A client application ("app") authorized to call the API (MLS-203). MLS itself is treated as an
 * app like any other.
 *
 * The API key handed out to clients has the form `{keyId}.{secret}`: [keyId] is a public,
 * non-secret identifier used for fast lookup, while only the hash of the secret part
 * ([secretHash]) is kept - the raw secret is never stored.
 */
data class ApiKey(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val keyId: String,
    val secretHash: String,
    val namespace: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val isEnabled: Boolean = true,
)
