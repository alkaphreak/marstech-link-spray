package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.entity.ApiKeyEntity

/**
 * Result of creating a new API key: carries the raw key (`{keyId}.{secret}`) so it can be
 * displayed to the admin exactly once. The raw secret is never persisted (only its BCrypt hash
 * is stored on [apiKey]).
 */
data class ApiKeyCreationResult(
    val apiKey: ApiKeyEntity,
    val rawKey: String,
)
