package fr.marstech.mtlinkspray.adapter.outbound.mongo.signature
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WriteSignatureRepository : MongoRepository<WriteSignatureEntity, String> {
    fun findTop50ByResourceIdContainingOrderByCreatedAtDesc(resourceId: String): List<WriteSignatureEntity>
}
