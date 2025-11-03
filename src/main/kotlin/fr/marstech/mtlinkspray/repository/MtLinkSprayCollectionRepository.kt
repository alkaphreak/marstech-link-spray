package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.MtLinkSprayCollectionItem
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MtLinkSprayCollectionRepository : MongoRepository<MtLinkSprayCollectionItem, String> {
    override fun findById(id: String): Optional<MtLinkSprayCollectionItem>
}
