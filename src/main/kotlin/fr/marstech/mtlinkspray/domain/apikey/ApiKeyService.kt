package fr.marstech.mtlinkspray.domain.apikey

import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver

/** Admin operations on API keys; key resolution for other domains is [ApiKeyResolver]. */
interface ApiKeyService {
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
    fun listApiKeys(): List<ApiKey>

    /**
     * Enables or disables an existing API key. Throws [NoSuchElementException] if not found.
     */
    fun setEnabled(id: String, isEnabled: Boolean): ApiKey

    /**
     * Replaces the secret of an existing API key, keeping its keyId and namespace: the previous
     * secret stops working at once. Throws [NoSuchElementException] if not found.
     */
    fun regenerateSecret(id: String): ApiKeyCreationResult
}
