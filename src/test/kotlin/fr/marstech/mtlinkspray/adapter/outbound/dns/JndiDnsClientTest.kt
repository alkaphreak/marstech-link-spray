package fr.marstech.mtlinkspray.adapter.outbound.dns
import fr.marstech.mtlinkspray.adapter.outbound.dns.JndiDnsClient.Companion.toDomain
import fr.marstech.mtlinkspray.domain.devtools.DnsQueryException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.naming.CommunicationException
import javax.naming.NameNotFoundException

class JndiDnsClientTest {

    @Test
    fun shouldThrowDnsQueryExceptionWhenServerDoesNotAnswer() {
        // Given: nothing listens on the discard port of the loopback interface
        val client = JndiDnsClient()

        // When
        val e = assertThrows(DnsQueryException::class.java) { client.query("127.0.0.1:9", "example.com", "A") }

        // Then
        assertFalse(e.nxdomain)
    }

    @Test
    fun shouldMapNameNotFoundToNxdomainWithItsExplanation() {
        // When
        val e = NameNotFoundException("DNS name not found").toDomain()

        // Then
        assertEquals("DNS name not found", e.reason)
        assertTrue(e.nxdomain)
    }

    @Test
    fun shouldFallBackToExceptionNameWhenNamingErrorHasNoExplanation() {
        // When
        val e = CommunicationException().toDomain()

        // Then
        assertEquals("CommunicationException", e.reason)
        assertFalse(e.nxdomain)
    }

    @Test
    fun shouldRejectJndiUrlBeforeAnyLookup() {
        // Given
        val client = JndiDnsClient()

        // When / Then
        assertThrows(IllegalArgumentException::class.java) { client.query("127.0.0.1:9", "ldap://attacker/x", "A") }
    }
}
