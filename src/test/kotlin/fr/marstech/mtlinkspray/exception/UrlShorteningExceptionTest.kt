package fr.marstech.mtlinkspray.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UrlShorteningExceptionTest {

    @Test
    fun `should create exception with message and cause`() {
        val cause = RuntimeException("Root cause")
        val exception = UrlShorteningException("Error occurred", cause)
        assertEquals("Error occurred", exception.message)
        assertEquals(cause, exception.cause)
    }
}
