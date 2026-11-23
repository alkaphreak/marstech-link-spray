package fr.marstech.mtlinkspray.adapter.inbound.web.apikey
import fr.marstech.mtlinkspray.adapter.outbound.mongo.apikey.ApiKeyEntity

/**
 * Result of creating a new API key: carries the raw key (`{keyId}.{secret}`) so it can be
 * displayed to the admin exactly once. The raw secret is never persisted (only its BCrypt hash
 * is stored on [apiKey]).
 */
data class ApiKeyCreationResult(
    val apiKey: ApiKeyEntity,
    val rawKey: String,
)
