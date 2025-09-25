package fr.marstech.mtlinkspray.conf

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.MongoTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Configuration
open class ApplicationReadyEventHandler(private val mongoTemplate: MongoTemplate) {

    @EventListener(ApplicationReadyEvent::class)
    fun appReadyhandler() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                log.info("Application is ready. Checking MongoDB connection...")
                mongoTemplate.executeCommand("{ ping: 1 }")
                log.info("Successfully connected to MongoDB.")
            } catch (e: Exception) {
                log.error("Failed to connect to MongoDB.", e)
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationReadyEventHandler::class.java)
    }
}
