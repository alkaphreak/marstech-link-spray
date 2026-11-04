package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.service.ApiKeyService
import fr.marstech.mtlinkspray.service.WriteSignatureService
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Renders every Thymeleaf view through the full context (MLS-245): the per-controller unit tests
 * only check the view name, so a template that fails to parse (MLS-241: `[[...]]` in qr.html's
 * script read as an inline expression) still passed them.
 */
@SpringBootTest
@AutoConfigureMockMvc
internal class ViewRenderingTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    // No MongoDB in the test profile: the admin view lists API keys, so stub the service.
    @MockitoBean
    private lateinit var apiKeyService: ApiKeyService

    @MockitoBean
    private lateinit var writeSignatureService: WriteSignatureService

    @Value("\${mt.link-spray.admin.username}")
    private lateinit var adminUsername: String

    @Value("\${mt.link-spray.admin.password}")
    private lateinit var adminPassword: String

    @ParameterizedTest
    @CsvSource(
        "/, site-menu",
        "/qr, qrForm",
        "/spray, inputLinkList",
        "/mailto, mailtoForm",
        "/dns, dnsForm",
        "/ip, ipInfo",
        "/fingerprint, fingerprintRun",
        "/paste, inputPasteBinTextArea",
        "/random, randomForm",
        "/uuid, uuid-value",
        "/shortener, input-link",
        "/dashboard, input-dashboard-name",
        "/abuse, inputAbuseDecsription",
        "/privacy, privacy-retention",
    )
    fun shouldRenderPublicView(path: String, elementId: String) {
        // Given / When / Then
        mockMvc.perform(get(path))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("id=\"$elementId\"")))
    }

    @Test
    fun shouldRenderAdminApiKeysView() {
        // Given / When / Then
        mockMvc.perform(get("/admin/api-keys").with(httpBasic(adminUsername, adminPassword)))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("id=\"input-namespace\"")))
    }

    @Test
    fun shouldRenderAdminSignaturesView() {
        // Given / When / Then
        mockMvc.perform(get("/admin/signatures?resourceId=abc").with(httpBasic(adminUsername, adminPassword)))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("No signature for this resource.")))
    }

    @Test
    fun shouldPutFingerprintFieldOnWriteForms() {
        // Given / When / Then
        listOf("/shortener", "/paste").forEach { path ->
            mockMvc.perform(get(path))
                .andExpect(content().string(containsString("data-fingerprint")))
                .andExpect(content().string(containsString("name=\"fp\"")))
        }
    }
}
