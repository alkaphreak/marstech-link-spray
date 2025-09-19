package fr.marstech.mtlinkspray.conf

import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.MtLinkSprayCollectionItem
import fr.marstech.mtlinkspray.repository.MtLinkSprayCollectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import java.time.LocalDateTime
import java.util.*

@Configuration
@Profile("dev")
open class ApplicationReadyEventHandlerForDev(
    private val environment: Environment, private val mtLinkSprayCollectionRepository: MtLinkSprayCollectionRepository
) {
    @Value($$"${mt.link-spray.version}")
    private lateinit var mtLinkSprayVersion: String

    private val scope = CoroutineScope(Dispatchers.Default)

    @EventListener(ApplicationReadyEvent::class)
    internal fun displayServerUrlInConsole() = scope.launch {
        kotlinx.coroutines.delay(3000)
        log.info("Local server : http://localhost:${environment.getProperty("server.port")}")
    }

    @EventListener(ApplicationReadyEvent::class)
    internal fun testMongoDbConnection() = scope.launch {
        kotlinx.coroutines.delay(4000)
        val mongoDbUriEnvVar = environment.getProperty("spring.data.mongodb.uri")
        log.info("MongoDB URI : $mongoDbUriEnvVar")

        val uuid = UUID.randomUUID().toString()
        val item = MtLinkSprayCollectionItem(
            id = uuid,
            creationDate = LocalDateTime.now(),
            expiresAt = null,
            isEnabled = true,
            description = "Test item for MongoDB connection",
            metadata = mutableMapOf("testKey" to "testValue"),
            author = HistoryItem(
                ipAddress = null,
                dateTime = LocalDateTime.now(),
                action = "Created for MongoDB connection test"
            ),
            historyItems = mutableListOf()
        )

        try {
            mtLinkSprayCollectionRepository.save(item)
            val found = mtLinkSprayCollectionRepository.findById(uuid)
            if (found.isPresent) {
                log.info("MongoDB test item found: $found")
            } else {
                log.warn("MongoDB test item not found")
            }
            mtLinkSprayCollectionRepository.findAll().forEach { log.info(it.toString()) }
        } catch (e: Exception) {
            log.error("MongoDB connection test failed: ${e.message}", e)
        }
        log.info("MongoDB connection test completed")
    }

    @EventListener(ApplicationReadyEvent::class)
    internal fun displayAppVersion() = scope.launch {
        kotlinx.coroutines.delay(5000)
        log.info("App version : $mtLinkSprayVersion")
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationReadyEventHandlerForDev::class.java)
    }
}