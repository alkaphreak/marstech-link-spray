package fr.marstech.mtlinkspray.service

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import javax.naming.NamingException

class JndiDnsClientTest {

    @Test
    fun shouldThrowNamingExceptionWhenServerDoesNotAnswer() {
        // Given: nothing listens on the discard port of the loopback interface
        val client = JndiDnsClient()

        // When / Then
        assertThrows(NamingException::class.java) { client.query("127.0.0.1:9", "example.com", "A") }
    }
}
