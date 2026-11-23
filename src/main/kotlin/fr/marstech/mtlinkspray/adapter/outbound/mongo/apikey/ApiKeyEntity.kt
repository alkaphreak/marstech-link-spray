package fr.marstech.mtlinkspray.adapter.outbound.mongo.apikey

import fr.marstech.mtlinkspray.domain.apikey.ApiKey
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

/** Stored form of [ApiKey]. */
@Document(collection = "api_keys")
@TypeAlias("fr.marstech.mtlinkspray.entity.ApiKeyEntity")
data class ApiKeyEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val name: String,
    @Indexed(unique = true) val keyId: String,
    val secretHash: String,
    @Indexed(unique = true) val namespace: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    var isEnabled: Boolean = true,
) {
    fun toDomain() = ApiKey(id, name, keyId, secretHash, namespace, creationDate, isEnabled)

    companion object {
        fun of(apiKey: ApiKey) = with(apiKey) {
            ApiKeyEntity(id, name, keyId, secretHash, namespace, creationDate, isEnabled)
        }
    }
}
