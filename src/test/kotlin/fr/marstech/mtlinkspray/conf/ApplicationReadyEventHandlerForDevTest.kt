package fr.marstech.mtlinkspray.conf

import fr.marstech.mtlinkspray.entity.MtLinkSprayCollectionItem
import fr.marstech.mtlinkspray.repository.MtLinkSprayCollectionRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.core.env.Environment
import java.util.*

class ApplicationReadyEventHandlerForDevTest {

    private lateinit var environment: Environment
    private lateinit var repository: MtLinkSprayCollectionRepository
    private lateinit var handler: ApplicationReadyEventHandlerForDev

    @BeforeEach
    fun setup() {
        environment = mock(Environment::class.java)
        repository = mock(MtLinkSprayCollectionRepository::class.java)
        handler = ApplicationReadyEventHandlerForDev(environment, repository)
    }

    @Test
    fun displayServerUrlInConsoleLogsServerUrl() {
        runBlocking {
            `when`(environment.getProperty("server.port")).thenReturn("8080")
            handler.displayServerUrlInConsole()
            // Check logs manually or with a log-capturing library
        }
    }

    @Test
    fun testMongoDbConnectionSavesAndFindsItem() {
        runBlocking {
            val item = mock(MtLinkSprayCollectionItem::class.java)
            `when`(repository.save(any())).thenReturn(item)
            `when`(repository.findById(anyString())).thenReturn(Optional.of(item))
            `when`(repository.findAll()).thenReturn(listOf(item))
            `when`(environment.getProperty("spring.data.mongodb.uri")).thenReturn("mongodb://localhost:27017/test")

            handler.testMongoDb()
            verify(repository).save(any())
            verify(repository).findById(anyString())
            verify(repository).findAll()
        }
    }

    @Test
    fun displayAppVersionLogsAppVersion() {
        runBlocking {
            // You may need to set the private field via reflection if needed
            handler.displayAppVersion()
            // Check logs manually or with a log-capturing library
        }
    }
}