package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.ApiKeyCreationResult
import fr.marstech.mtlinkspray.entity.ApiKeyEntity

interface ApiKeyService {
    /**
     * Resolves and validates a raw API key (as received in the `X-Api-Key` header).
     * Throws [IllegalAccessException] if the key is unknown, disabled, or blank.
     */
    fun resolve(rawApiKey: String?): ApiKeyEntity

    /**
     * Creates a new API key for app [name] under [namespace]. Per MLS-203 decision, this is
     * only ever called from the admin page (`/admin/api-keys`), never self-service.
     * Throws [IllegalArgumentException] if [name]/[namespace] are blank or [namespace] is
     * already taken by another app.
     */
    fun createApiKey(name: String, namespace: String): ApiKeyCreationResult

    /**
     * Lists all registered API keys (admin view).
     */
    fun listApiKeys(): List<ApiKeyEntity>

    /**
     * Enables or disables an existing API key. Throws [NoSuchElementException] if not found.
     */
    fun setEnabled(id: String, isEnabled: Boolean): ApiKeyEntity
}
