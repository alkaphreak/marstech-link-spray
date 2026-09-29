package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.ApiKeyCreationResult
import fr.marstech.mtlinkspray.entity.ApiKeyEntity
import fr.marstech.mtlinkspray.repository.ApiKeyRepository
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
        return ApiKeyCreationResult(apiKey = apiKey, rawKey = "$keyId.$secret")
    }

    override fun listApiKeys(): List<ApiKeyEntity> = apiKeyRepository.findAll()

    override fun setEnabled(id: String, isEnabled: Boolean): ApiKeyEntity {
        val apiKey = apiKeyRepository.findById(id)
            .orElseThrow { NoSuchElementException("API key $id not found") }
        apiKey.isEnabled = isEnabled
        return apiKeyRepository.save(apiKey)
    }

    private fun generateKeyId(): String = UUID.randomUUID().toString().replace("-", "").take(12)

    private fun generateSecret(): String =
        (1..SECRET_LENGTH).map { SECRET_CHARSET[secureRandom.nextInt(SECRET_CHARSET.length)] }.joinToString("")

    companion object {
        private const val SECRET_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        private const val SECRET_LENGTH = 32
    }
}
