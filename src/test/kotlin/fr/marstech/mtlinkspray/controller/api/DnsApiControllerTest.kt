package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.dto.DnsLookupResponse
import fr.marstech.mtlinkspray.dto.DnsPropagationResponse
import fr.marstech.mtlinkspray.service.DnsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [DnsApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class DnsApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var dnsService: DnsService

    @Test
    fun shouldReturnLookupWithDefaultTypeA() {
        // Given
        `when`(dnsService.lookup(EXAMPLE, "A")).thenReturn(response(EXAMPLE, "A"))

        // When / Then
        mockMvc.perform(get(DNS_PATH).param("name", EXAMPLE))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resolver").value("Cloudflare"))
            .andExpect(jsonPath("$.records[0]").value("1.2.3.4"))
    }

    @Test
    fun shouldAcceptUnderscoreLabelForTxtLookup() {
        // Given
        `when`(dnsService.lookup("_dmarc.example.com", "txt")).thenReturn(response("_dmarc.example.com", "TXT"))

        // When / Then
        mockMvc.perform(get(DNS_PATH).param("name", "_dmarc.example.com").param("type", "txt"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.type").value("TXT"))
    }

    @Test
    fun shouldAcceptPunycodeDomain() {
        // Given
        `when`(dnsService.lookup(PUNYCODE, "A")).thenReturn(response(PUNYCODE, "A"))

        // When / Then
        mockMvc.perform(get(DNS_PATH).param("name", PUNYCODE)).andExpect(status().isOk)
    }

    @ParameterizedTest
    @CsvSource("name, ''", "name, not a domain", "name, localhost", "name, ldap://attacker/x", "type, SOA")
    fun shouldRejectInvalidLookupParameter(param: String, value: String) {
        // Given
        val request = get(DNS_PATH).param("name", EXAMPLE).param(param, value)

        // When / Then
        mockMvc.perform(request).andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnReverseLookup() {
        // Given
        `when`(dnsService.reverse("8.8.8.8")).thenReturn(response("8.8.8.8.in-addr.arpa", "PTR"))

        // When / Then
        mockMvc.perform(get("$DNS_PATH/reverse").param("ip", "8.8.8.8"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.type").value("PTR"))
    }

    @ParameterizedTest
    @CsvSource("''", "example.com", "256.1.1.1")
    fun shouldRejectReverseLookupOfNonIp(ip: String) {
        // Given / When / Then
        mockMvc.perform(get("$DNS_PATH/reverse").param("ip", ip)).andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnPropagationResults() {
        // Given
        `when`(dnsService.propagation(EXAMPLE, "A"))
            .thenReturn(DnsPropagationResponse(EXAMPLE, "A", listOf(response(EXAMPLE, "A")), complete = true, consistent = true))

        // When / Then
        mockMvc.perform(get("$DNS_PATH/propagation").param("name", EXAMPLE))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.consistent").value(true))
            .andExpect(jsonPath("$.results[0].resolver").value("Cloudflare"))
    }

    private fun response(name: String, type: String) =
        DnsLookupResponse(name, type, "Cloudflare", listOf("1.2.3.4"), timeMs = 12)

    companion object {
        private const val DNS_PATH = "/api/dns"
        private const val EXAMPLE = "example.com"
        private const val PUNYCODE = "xn--e1afmkfd.xn--p1ai"
    }
}
