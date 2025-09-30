package fr.marstech.mtlinkspray.conf

import fr.marstech.mtlinkspray.repository.MtLinkSprayCollectionRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.core.env.Environment

class ApplicationReadyEventHandlerForDevTest {

    private lateinit var environment: Environment
    private lateinit var repository: MtLinkSprayCollectionRepository
    private lateinit var handler: ApplicationReadyEventHandlerForDev

    @BeforeEach
    fun setup() {
        environment = mock(Environment::class.java)
        repository = mock(MtLinkSprayCollectionRepository::class.java)
        handler = ApplicationReadyEventHandlerForDev(environment)
    }

    @Test
    fun displayServerUrlInConsoleLogsServerUrl() {
        runBlocking {
            `when`(environment.getProperty("server.port")).thenReturn("8080")
            handler.displayServerUrlInConsole()
            // Check logs manually or with a log-capturing library
        }
    }
}