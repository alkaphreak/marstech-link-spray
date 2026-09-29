package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.ApiKeyEntity
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface ApiKeyRepository : MongoRepository<ApiKeyEntity, String> {
    fun findByKeyId(keyId: String): Optional<ApiKeyEntity>
    fun findByNamespace(namespace: String): Optional<ApiKeyEntity>
}
