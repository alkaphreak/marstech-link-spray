package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.WriteSignatureEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WriteSignatureRepository : MongoRepository<WriteSignatureEntity, String> {
    fun findTop50ByResourceIdContainingOrderByCreatedAtDesc(resourceId: String): List<WriteSignatureEntity>
}
