package fr.marstech.mtlinkspray.mcp

import fr.marstech.mtlinkspray.dto.DnsLookupRequest
import fr.marstech.mtlinkspray.dto.DnsLookupResponse
import fr.marstech.mtlinkspray.dto.DnsReverseRequest
import fr.marstech.mtlinkspray.dto.IpInfoResponse
import fr.marstech.mtlinkspray.dto.QrCodeRequest
import fr.marstech.mtlinkspray.dto.RandomNumberResponse
import fr.marstech.mtlinkspray.service.DnsService
import fr.marstech.mtlinkspray.service.IpService
import fr.marstech.mtlinkspray.service.QrCodeService
import fr.marstech.mtlinkspray.service.RandomNumberService
import io.modelcontextprotocol.spec.McpSchema.CallToolResult
import io.modelcontextprotocol.spec.McpSchema.ImageContent
import jakarta.validation.Validator
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.UUID

/**
 * Read-only sol4.space tools exposed on /mcp (MLS-266, docs/adr/0004). Tools with a REST request DTO
 * run Bean Validation on it, and random_number goes through the service's own checks like
 * GET /api/random, so MCP and REST refuse the same values. DNS propagation is left out: it queries
 * every public resolver on each call.
 */
@Component
class Sol4McpTools(
    private val dnsService: DnsService,
    private val ipService: IpService,
    private val qrCodeService: QrCodeService,
    private val randomNumberService: RandomNumberService,
    private val validator: Validator,
) {

    @McpTool(name = "uuid", description = "Generate a random UUID (version 4).")
    fun uuid(): String = UUID.randomUUID().toString()

    @McpTool(name = "random_number", description = "Random integer between min (default 0) and max, both inclusive.")
    fun randomNumber(
        // Nullable so a client that omits max gets the service's message, not a reflection error
        @McpToolParam(description = "Upper bound", required = true) max: Int?,
        @McpToolParam(description = "Lower bound, 0 when omitted", required = false) min: Int?,
    ): RandomNumberResponse = randomNumberService.generate(min?.toString(), max?.toString())

    @McpTool(name = "dns_lookup", description = "DNS records of a domain name, through Cloudflare's public resolver.")
    fun dnsLookup(
        @McpToolParam(description = "Domain name, e.g. example.com", required = true) name: String,
        @McpToolParam(description = "Record type: A, AAAA, MX, TXT, CNAME or NS (default A)", required = false) type: String?,
    ): DnsLookupResponse {
        val request = valid(DnsLookupRequest(name = name, type = type ?: "A"))
        return dnsService.lookup(request.name.orEmpty(), request.type)
    }

    @McpTool(name = "dns_reverse", description = "Reverse DNS (PTR record) of an IPv4 or IPv6 address.")
    fun dnsReverse(
        @McpToolParam(description = "IPv4 or IPv6 address", required = true) ip: String,
    ): DnsLookupResponse = dnsService.reverse(valid(DnsReverseRequest(ip = ip)).ip.orEmpty())

    /**
     * Unlike GET /api/ip, the address is required: the MCP caller's own address means nothing to a
     * tool. Validated with [DnsReverseRequest], the same IP pattern as IpLookupRequest plus @NotBlank.
     */
    @McpTool(name = "ip_lookup", description = "Geolocation and network owner of an IPv4 or IPv6 address.")
    fun ipLookup(
        @McpToolParam(description = "IPv4 or IPv6 address", required = true) ip: String,
    ): IpInfoResponse = ipService.lookup(valid(DnsReverseRequest(ip = ip)).ip.orEmpty())

    @McpTool(name = "qr_code", description = "QR code of a text, as a PNG image.")
    fun qrCode(
        @McpToolParam(description = "Text to encode, at most 2000 characters", required = true) text: String,
        @McpToolParam(description = "Size in pixels, 64 to 1024 (default 256)", required = false) size: Int?,
    ): CallToolResult {
        val png = qrCodeService.render(valid(QrCodeRequest(text = text, size = size ?: 256, format = "png")))
        return CallToolResult.builder()
            .addContent(ImageContent(null, Base64.getEncoder().encodeToString(png), "image/png"))
            .build()
    }

    private fun <T : Any> valid(request: T): T {
        val violations = validator.validate(request)
        require(violations.isEmpty()) { violations.map { it.message }.sorted().joinToString("; ") }
        return request
    }
}
