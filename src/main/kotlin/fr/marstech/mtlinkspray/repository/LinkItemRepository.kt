package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.LinkItem
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface LinkItemRepository : MongoRepository<LinkItem, String> {

    @Query(value = "{}", fields = "{ '_id' : 1 }")
    fun findAllIds(): List<String>
}
