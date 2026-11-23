package fr.marstech.mtlinkspray.domain.apikey

/**
 * Result of creating a new API key: carries the raw key (`{keyId}.{secret}`) so it can be
 * displayed to the admin exactly once. The raw secret is never persisted (only its hash is
 * stored on [apiKey]).
 */
data class ApiKeyCreationResult(
    val apiKey: ApiKey,
    val rawKey: String,
)
