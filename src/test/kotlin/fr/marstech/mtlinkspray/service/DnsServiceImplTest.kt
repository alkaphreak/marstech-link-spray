package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.enums.PublicDnsResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.naming.NameNotFoundException

class DnsServiceImplTest {

    @Test
    fun shouldReturnRecordsFromCloudflareWithUppercasedType() {
        // Given
        val calls = mutableListOf<Triple<String, String, String>>()
        val service = DnsServiceImpl { server, name, type -> calls += Triple(server, name, type); listOf("93.184.215.14") }

        // When
        val response = service.lookup(EXAMPLE, "a")

        // Then
        assertEquals(listOf(Triple("1.1.1.1", EXAMPLE, "A")), calls)
        assertEquals("Cloudflare", response.resolver)
        assertEquals(listOf("93.184.215.14"), response.records)
        assertNull(response.error)
    }

    @Test
    fun shouldReportNamingErrorInsteadOfThrowing() {
        // Given
        val service = DnsServiceImpl { _, _, _ -> throw NameNotFoundException("DNS name not found") }

        // When
        val response = service.lookup("nope.example", "MX")

        // Then
        assertTrue(response.records.isEmpty())
        assertEquals("DNS name not found", response.error)
    }

    @Test
    fun shouldFallBackToExceptionNameWhenNamingErrorHasNoExplanation() {
        // Given
        val service = DnsServiceImpl { _, _, _ -> throw NameNotFoundException() }

        // When / Then
        assertEquals("NameNotFoundException", service.lookup(EXAMPLE, "A").error)
    }

    @Test
    fun shouldQueryEveryPublicResolverAndReportConsistency() {
        // Given
        val service = DnsServiceImpl { _, _, _ -> listOf("b", "a") }

        // When
        val response = service.propagation(EXAMPLE, "txt")

        // Then
        assertEquals(PublicDnsResolver.entries.map { it.label }, response.results.map { it.resolver })
        assertEquals("TXT", response.type)
        assertTrue(response.consistent)
    }

    @Test
    fun shouldReportInconsistentWhenResolversDisagreeIgnoringFailures() {
        // Given
        val service = DnsServiceImpl { server, _, _ ->
            when (server) {
                PublicDnsResolver.GOOGLE.address -> listOf("old")
                PublicDnsResolver.QUAD9.address -> throw NameNotFoundException("timeout")
                else -> listOf("new")
            }
        }

        // When
        val response = service.propagation(EXAMPLE, "A")

        // Then
        assertFalse(response.consistent)
        assertEquals("timeout", response.results.first { it.resolver == "Quad9" }.error)
    }

    @Test
    fun shouldQueryPtrRecordOfReversedIpv4() {
        // Given
        val service = DnsServiceImpl { _, name, type -> listOf("$type $name") }

        // When
        val response = service.reverse("8.8.4.4")

        // Then
        assertEquals("PTR", response.type)
        assertEquals(listOf("PTR 4.4.8.8.in-addr.arpa"), response.records)
    }

    @Test
    fun shouldBuildIp6ArpaNameFromIpv6() {
        // Given / When
        val name = DnsServiceImpl.ptrName("2001:db8::1")

        // Then
        assertEquals("1." + "0.".repeat(23) + "8.b.d.0.1.0.0.2.ip6.arpa", name)
    }

    @Test
    fun shouldRejectInvalidIpLiteral() {
        // Given / When / Then
        assertThrows(IllegalArgumentException::class.java) { DnsServiceImpl.ptrName("2001:::zz") }
    }

    companion object {
        private const val EXAMPLE = "example.com"
    }
}
