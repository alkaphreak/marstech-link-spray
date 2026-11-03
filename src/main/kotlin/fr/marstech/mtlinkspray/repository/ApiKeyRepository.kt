package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.ApiKeyEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Update
import java.util.*

interface ApiKeyRepository : MongoRepository<ApiKeyEntity, String> {
    fun findByKeyId(keyId: String): Optional<ApiKeyEntity>
    fun findByNamespace(namespace: String): Optional<ApiKeyEntity>

    // Single-field updates: a full save() of a stale document would revert a concurrent change
    // (e.g. a toggle restoring a just-rotated secret). Both return the matched count.
    @Update("{ '\$set': { 'secretHash': ?1 } }")
    fun findAndSetSecretHashById(id: String, secretHash: String): Long

    @Update("{ '\$set': { 'isEnabled': ?1 } }")
    fun findAndSetIsEnabledById(id: String, isEnabled: Boolean): Long
}
