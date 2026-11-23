package fr.marstech.mtlinkspray.domain.apikey

interface ApiKeyStore {
    fun findById(id: String): ApiKey?

    fun findByKeyId(keyId: String): ApiKey?

    fun findByNamespace(namespace: String): ApiKey?

    fun findAll(): List<ApiKey>

    fun save(apiKey: ApiKey): ApiKey

    // Single-field updates: a full save() of a stale key would revert a concurrent change
    // (e.g. a toggle restoring a just-rotated secret). Both return false when no key has [id].
    fun updateSecretHash(id: String, secretHash: String): Boolean

    fun updateEnabled(id: String, isEnabled: Boolean): Boolean
}
