package fr.marstech.mtlinkspray.domain.apikey
import fr.marstech.mtlinkspray.domain.shared.ApiCaller
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import fr.marstech.mtlinkspray.domain.shared.PasswordHasher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*

/**
 * Validates API keys against the stored secret hashes, and provisions new keys for the admin
 * page (`/admin/api-keys`). Per MLS-203 decision, key creation is admin-only, never self-service.
 *
 * Raw API keys have the form `{keyId}.{secret}` allowing an indexed O(1) lookup by [keyId]
 * before verifying the secret against its hash.
 */
@Service
class ApiKeyService(
    private val apiKeyStore: ApiKeyStore,
    private val passwordHasher: PasswordHasher,
) : ApiKeyResolver {

    private val secureRandom = SecureRandom()

    override fun resolve(rawApiKey: String?): ApiCaller {
        if (rawApiKey.isNullOrBlank()) throw IllegalAccessException("Missing API key")

        val (keyId, secret) = rawApiKey.split(".", limit = 2)
            .takeIf { it.size == 2 }
            ?.let { it[0] to it[1] }
            ?: throw IllegalAccessException("Malformed API key")

        val apiKey = apiKeyStore.findByKeyId(keyId) ?: throw IllegalAccessException("Invalid API key")

        if (!apiKey.isEnabled || !passwordHasher.matches(secret, apiKey.secretHash)) {
            throw IllegalAccessException("Invalid API key")
        }

        return ApiCaller(apiKeyId = apiKey.id, namespace = apiKey.namespace)
    }

    override fun isNamespaceEnabled(namespace: String): Boolean =
        apiKeyStore.findByNamespace(namespace)?.isEnabled ?: false

    /**
     * Creates a new API key for app [name] under [namespace]. Per MLS-203 decision, this is
     * only ever called from the admin page (`/admin/api-keys`), never self-service.
     * Throws [IllegalArgumentException] if [name]/[namespace] are blank or [namespace] is
     * already taken by another app.
     */
    fun createApiKey(name: String, namespace: String): ApiKeyCreationResult {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(namespace.isNotBlank()) { "Namespace cannot be blank" }
        require(apiKeyStore.findByNamespace(namespace) == null) { "Namespace '$namespace' is already in use" }

        val keyId = generateKeyId()
        val secret = generateSecret()
        val apiKey = apiKeyStore.save(
            ApiKey(
                name = name,
                keyId = keyId,
                secretHash = passwordHasher.hash(secret),
                namespace = namespace,
            )
        )
        log.info("API key created: id={}, keyId={}, namespace={}, name={}", apiKey.id, keyId, namespace.forLog(), name.forLog())
        return ApiKeyCreationResult(apiKey = apiKey, rawKey = "$keyId.$secret")
    }

    /**
     * Lists all registered API keys (admin view).
     */
    fun listApiKeys(): List<ApiKey> = apiKeyStore.findAll()

    /**
     * Enables or disables an existing API key. Throws [NoSuchElementException] if not found.
     */
    fun setEnabled(id: String, isEnabled: Boolean): ApiKey {
        if (!apiKeyStore.updateEnabled(id, isEnabled)) throw notFound(id)
        val apiKey = apiKeyStore.findById(id) ?: throw notFound(id)
        log.info("API key enabled set: id={}, keyId={}, namespace={}, enabled={}", id, apiKey.keyId, apiKey.namespace.forLog(), isEnabled)
        return apiKey
    }

    /**
     * Replaces the secret of an existing API key, keeping its keyId and namespace: the previous
     * secret stops working at once. Throws [NoSuchElementException] if not found.
     */
    fun regenerateSecret(id: String): ApiKeyCreationResult {
        val apiKey = apiKeyStore.findById(id) ?: throw notFound(id)
        val secret = generateSecret()
        val secretHash = passwordHasher.hash(secret)
        if (!apiKeyStore.updateSecretHash(id, secretHash)) throw notFound(id)
        log.info("API key secret regenerated: id={}, keyId={}, namespace={}", id, apiKey.keyId, apiKey.namespace.forLog())
        return ApiKeyCreationResult(apiKey = apiKey.copy(secretHash = secretHash), rawKey = "${apiKey.keyId}.$secret")
    }

    /** Escapes CR/LF so an admin-supplied value cannot forge extra log lines. */
    private fun String.forLog(): String = replace("\r", "\\r").replace("\n", "\\n")

    private fun notFound(id: String) = NoSuchElementException("API key $id not found")

    private fun generateKeyId(): String = UUID.randomUUID().toString().replace("-", "").take(12)

    private fun generateSecret(): String =
        (1..SECRET_LENGTH).map { SECRET_CHARSET[secureRandom.nextInt(SECRET_CHARSET.length)] }.joinToString("")

    companion object {
        private val log = LoggerFactory.getLogger(ApiKeyService::class.java)
        private const val SECRET_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        private const val SECRET_LENGTH = 32
    }
}
