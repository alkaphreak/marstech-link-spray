package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.ImageEntity
import org.springframework.data.mongodb.repository.MongoRepository

interface ImageRepository : MongoRepository<ImageEntity, String> {
    fun findByApiKeyId(apiKeyId: String): List<ImageEntity>
}
