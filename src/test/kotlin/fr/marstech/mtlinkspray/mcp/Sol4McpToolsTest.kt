package fr.marstech.mtlinkspray.mcp

import fr.marstech.mtlinkspray.dto.DnsLookupResponse
import fr.marstech.mtlinkspray.dto.IpInfoResponse
import fr.marstech.mtlinkspray.dto.QrCodeRequest
import fr.marstech.mtlinkspray.dto.RandomNumberResponse
import fr.marstech.mtlinkspray.service.DnsService
import fr.marstech.mtlinkspray.service.IpService
import fr.marstech.mtlinkspray.service.QrCodeService
import fr.marstech.mtlinkspray.service.RandomNumberService
import io.modelcontextprotocol.spec.McpSchema.ImageContent
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.Base64
import java.util.UUID

class Sol4McpToolsTest {

    private val dnsService: DnsService = mock()
    private val ipService: IpService = mock()
    private val qrCodeService: QrCodeService = mock()
    private val randomNumberService: RandomNumberService = mock()
    private val tools = Sol4McpTools(
        dnsService, ipService, qrCodeService, randomNumberService,
        Validation.buildDefaultValidatorFactory().validator,
    )

    @Test
    fun uuidShouldReturnAParsableUuid() {
        assertEquals(4, UUID.fromString(tools.uuid()).version())
    }

    @Test
    fun randomNumberShouldPassBoundsAsStringsAndDefaultMinToNull() {
        val response: RandomNumberResponse = mock()
        whenever(randomNumberService.generate(null, "10")).thenReturn(response)

        assertSame(response, tools.randomNumber(10, null))
    }

    @Test
    fun randomNumberShouldPassAGivenMin() {
        val response: RandomNumberResponse = mock()
        whenever(randomNumberService.generate("3", "10")).thenReturn(response)

        assertSame(response, tools.randomNumber(10, 3))
    }

    @Test
    fun randomNumberShouldLetTheServiceRefuseAMissingMax() {
        val response: RandomNumberResponse = mock()
        whenever(randomNumberService.generate(null, null)).thenReturn(response)

        assertSame(response, tools.randomNumber(null, null))
    }

    @Test
    fun dnsLookupShouldDefaultTypeToA() {
        val response: DnsLookupResponse = mock()
        whenever(dnsService.lookup("example.com", "A")).thenReturn(response)

        assertSame(response, tools.dnsLookup("example.com", null))
    }

    @Test
    fun dnsLookupShouldRefuseAnInvalidNameLikeTheRestApi() {
        val error = assertThrows<IllegalArgumentException> { tools.dnsLookup("not a domain", "A") }

        assertEquals("Name must be a valid domain name", error.message)
        verifyNoInteractions(dnsService)
    }

    @Test
    fun dnsLookupShouldRefuseAnUnsupportedType() {
        assertThrows<IllegalArgumentException> { tools.dnsLookup("example.com", "SOA") }
        verifyNoInteractions(dnsService)
    }

    @Test
    fun dnsReverseShouldQueryAValidIp() {
        val response: DnsLookupResponse = mock()
        whenever(dnsService.reverse("1.1.1.1")).thenReturn(response)

        assertSame(response, tools.dnsReverse("1.1.1.1"))
    }

    @Test
    fun dnsReverseShouldRefuseAHostName() {
        assertThrows<IllegalArgumentException> { tools.dnsReverse("example.com") }
        verifyNoInteractions(dnsService)
    }

    @Test
    fun ipLookupShouldQueryAValidIpv6() {
        val response: IpInfoResponse = mock()
        whenever(ipService.lookup("2606:4700::1111")).thenReturn(response)

        assertSame(response, tools.ipLookup("2606:4700::1111"))
    }

    @Test
    fun ipLookupShouldRefuseABlankIpInsteadOfUsingTheCallerAddress() {
        assertThrows<IllegalArgumentException> { tools.ipLookup("") }
        verifyNoInteractions(ipService)
    }

    @Test
    fun qrCodeShouldReturnTheRenderedPngAsBase64ImageContent() {
        val png = byteArrayOf(1, 2, 3)
        whenever(qrCodeService.render(any())).thenReturn(png)

        val result = tools.qrCode("hello", null)

        val image = result.content().single() as ImageContent
        assertEquals("image/png", image.mimeType())
        assertTrue(png.contentEquals(Base64.getDecoder().decode(image.data())))
        val request = argumentCaptor<QrCodeRequest>().apply { verify(qrCodeService).render(capture()) }.firstValue
        assertEquals(256, request.size)
        assertEquals("png", request.format)
    }

    @Test
    fun qrCodeShouldRefuseAnOutOfRangeSize() {
        val error = assertThrows<IllegalArgumentException> { tools.qrCode("hello", 2048) }

        assertEquals("Size must not exceed 1024 pixels", error.message)
        verifyNoInteractions(qrCodeService)
    }
}
