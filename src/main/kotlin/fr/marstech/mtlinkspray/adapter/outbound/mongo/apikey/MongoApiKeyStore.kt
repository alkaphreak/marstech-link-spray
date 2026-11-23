package fr.marstech.mtlinkspray.adapter.outbound.mongo.apikey

import fr.marstech.mtlinkspray.domain.apikey.ApiKey
import fr.marstech.mtlinkspray.domain.apikey.ApiKeyStore
import org.springframework.stereotype.Component

@Component
class MongoApiKeyStore(
    private val repository: ApiKeyRepository,
) : ApiKeyStore {

    override fun findById(id: String): ApiKey? = repository.findById(id).orElse(null)?.toDomain()

    override fun findByKeyId(keyId: String): ApiKey? = repository.findByKeyId(keyId).orElse(null)?.toDomain()

    override fun findByNamespace(namespace: String): ApiKey? =
        repository.findByNamespace(namespace).orElse(null)?.toDomain()

    override fun findAll(): List<ApiKey> = repository.findAll().map { it.toDomain() }

    override fun save(apiKey: ApiKey): ApiKey = repository.save(ApiKeyEntity.of(apiKey)).toDomain()

    override fun updateSecretHash(id: String, secretHash: String): Boolean =
        repository.findAndSetSecretHashById(id, secretHash) > 0

    override fun updateEnabled(id: String, isEnabled: Boolean): Boolean =
        repository.findAndSetIsEnabledById(id, isEnabled) > 0
}
