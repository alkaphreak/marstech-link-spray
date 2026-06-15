package fr.marstech.mtlinkspray.conf

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.env.Environment

class ApplicationReadyEventHandlerForDevTest {

    private lateinit var environment: Environment
    private lateinit var handler: ApplicationReadyEventHandlerForDev

    @BeforeEach
    fun setup() {
        environment = mock()
        handler = ApplicationReadyEventHandlerForDev(environment, "1.0.0-test")
    }

    @Test
    fun displayServerUrlInConsoleLogsServerUrl() {
        // Given
        whenever(environment.getProperty("server.port")).thenReturn("8080")

        // When
        val job = handler.displayServerUrlInConsole()

        // Then - job is launched without exception
        runBlocking { job.join() }
    }
}