package fr.marstech.mtlinkspray.adapter.outbound.mongo.image
import org.springframework.data.mongodb.repository.MongoRepository

interface ImageRepository : MongoRepository<ImageEntity, String> {
    fun findByApiKeyId(apiKeyId: String): List<ImageEntity>
}
