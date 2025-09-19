package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.PasteEntity
import org.springframework.data.mongodb.repository.MongoRepository

interface PasteRepository : MongoRepository<PasteEntity, String> {
    fun findByIdAndIsPrivate(id: String, isPrivate: Boolean): PasteEntity?
}