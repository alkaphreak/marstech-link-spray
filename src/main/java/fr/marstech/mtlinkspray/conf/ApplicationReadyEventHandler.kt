package fr.marstech.mtlinkspray.conf

import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

@Configuration
open class ApplicationReadyEventHandler(private val mongoTemplate: MongoTemplate) {
    @EventListener(ApplicationReadyEvent::class)
    private fun dbMigrationHandler() {
        Thread.ofVirtual().start { this.dbMigration() }
    }

    private fun dbMigration() {
        log.info("Starting migration")

        // Find all documents as raw Documents
        val allItems = mongoTemplate.findAll(Document::class.java, "mtLinkSprayCollectionItem")
        log.info("Total items: {}", allItems.size)

        // Find by criteria
        val query = Query(Criteria.where("author").`is`(null as Any?))
        val activeItems = mongoTemplate.find(query, Document::class.java, "mtLinkSprayCollectionItem")

        log.info("Items with null author: {}", activeItems.size)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationReadyEventHandler::class.java)
    }
}
