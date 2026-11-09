package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.LinkItem
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.Update
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LinkItemRepository : MongoRepository<LinkItem, String> {

    @Query(value = "{}", fields = "{ '_id' : 1 }")
    fun findAllIds(): List<String>

    // Atomic $inc: a read-modify-write save() would lose concurrent clicks (MLS-261). Returns the matched count.
    @Update("{ '\$inc': { 'clickCount': 1 }, '\$set': { 'lastClickedAt': ?1 } }")
    fun findAndIncrementClickCountById(id: String, clickedAt: LocalDateTime): Long
}
