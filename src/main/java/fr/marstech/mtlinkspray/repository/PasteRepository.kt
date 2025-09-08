package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.Paste
import org.springframework.data.mongodb.repository.MongoRepository

interface PasteRepository : MongoRepository<Paste, String> {
    fun findByIdAndIsPrivate(id: String, isPrivate: Boolean): Paste?
}