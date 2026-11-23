package fr.marstech.mtlinkspray.adapter.outbound.mongo.paste
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PasteRepository : MongoRepository<PasteEntity, String>