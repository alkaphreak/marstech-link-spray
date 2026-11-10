package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.ApiKeyCreationResult
import fr.marstech.mtlinkspray.entity.ApiKeyEntity
import fr.marstech.mtlinkspray.repository.ApiKeyRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*

/**
 * Validates API keys against the stored BCrypt hashes, and provisions new keys for the admin
 * page (`/admin/api-keys`). Per MLS-203 decision, key creation is admin-only, never self-service.
 *
 * Raw API keys have the form `{keyId}.{secret}` allowing an indexed O(1) lookup by [keyId]
 * before verifying the secret against its BCrypt hash.
 */
@Service
class ApiKeyServiceImpl(
    private val apiKeyRepository: ApiKeyRepository
) : ApiKeyService {

    private val passwordEncoder = BCryptPasswordEncoder()
    private val secureRandom = SecureRandom()

    override fun resolve(rawApiKey: String?): ApiKeyEntity {
        if (rawApiKey.isNullOrBlank()) throw IllegalAccessException("Missing API key")

        val (keyId, secret) = rawApiKey.split(".", limit = 2)
            .takeIf { it.size == 2 }
            ?.let { it[0] to it[1] }
            ?: throw IllegalAccessException("Malformed API key")

        val apiKey = apiKeyRepository.findByKeyId(keyId)
            .orElseThrow { IllegalAccessException("Invalid API key") }

        if (!apiKey.isEnabled || !passwordEncoder.matches(secret, apiKey.secretHash)) {
            throw IllegalAccessException("Invalid API key")
        }

        return apiKey
    }

    override fun createApiKey(name: String, namespace: String): ApiKeyCreationResult {
        if (name.isBlank()) throw IllegalArgumentException("Name cannot be blank")
        if (namespace.isBlank()) throw IllegalArgumentException("Namespace cannot be blank")
        if (apiKeyRepository.findByNamespace(namespace).isPresent) {
            throw IllegalArgumentException("Namespace '$namespace' is already in use")
        }

        val keyId = generateKeyId()
        val secret = generateSecret()
        val apiKey = apiKeyRepository.save(
            ApiKeyEntity(
                name = name,
                keyId = keyId,
                secretHash = passwordEncoder.encode(secret)!!,
                namespace = namespace,
            )
        )
        log.info("API key created: id={}, keyId={}, namespace={}, name={}", apiKey.id, keyId, namespace, name)
        return ApiKeyCreationResult(apiKey = apiKey, rawKey = "$keyId.$secret")
    }

    override fun listApiKeys(): List<ApiKeyEntity> = apiKeyRepository.findAll()

    override fun setEnabled(id: String, isEnabled: Boolean): ApiKeyEntity {
        if (apiKeyRepository.findAndSetIsEnabledById(id, isEnabled) == 0L) throw notFound(id)
        val apiKey = apiKeyRepository.findById(id).orElseThrow { notFound(id) }
        log.info("API key enabled set: id={}, keyId={}, namespace={}, enabled={}", id, apiKey.keyId, apiKey.namespace, isEnabled)
        return apiKey
    }

    override fun regenerateSecret(id: String): ApiKeyCreationResult {
        val apiKey = apiKeyRepository.findById(id).orElseThrow { notFound(id) }
        val secret = generateSecret()
        val secretHash = passwordEncoder.encode(secret)!!
        if (apiKeyRepository.findAndSetSecretHashById(id, secretHash) == 0L) throw notFound(id)
        log.info("API key secret regenerated: id={}, keyId={}, namespace={}", id, apiKey.keyId, apiKey.namespace)
        return ApiKeyCreationResult(apiKey = apiKey.copy(secretHash = secretHash), rawKey = "${apiKey.keyId}.$secret")
    }

    private fun notFound(id: String) = NoSuchElementException("API key $id not found")

    private fun generateKeyId(): String = UUID.randomUUID().toString().replace("-", "").take(12)

    private fun generateSecret(): String =
        (1..SECRET_LENGTH).map { SECRET_CHARSET[secureRandom.nextInt(SECRET_CHARSET.length)] }.joinToString("")

    companion object {
        private val log = LoggerFactory.getLogger(ApiKeyServiceImpl::class.java)
        private const val SECRET_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        private const val SECRET_LENGTH = 32
    }
}
