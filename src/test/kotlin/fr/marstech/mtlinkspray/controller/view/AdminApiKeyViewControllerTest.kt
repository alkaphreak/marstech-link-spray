package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.dto.ApiKeyCreationResult
import fr.marstech.mtlinkspray.entity.ApiKeyEntity
import fr.marstech.mtlinkspray.service.ApiKeyService
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [AdminApiKeyViewController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(SecurityConfig::class)
class AdminApiKeyViewControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var apiKeyService: ApiKeyService

    private fun givenApiKey(): ApiKeyEntity = ApiKeyEntity(
        id = "id1",
        name = "Maeva App",
        keyId = "key123",
        secretHash = "hash",
        namespace = "maeva-app-1",
    )

    @Test
    fun getApiKeysShouldRequireAuthentication() {
        mockMvc.perform(get("/admin/api-keys"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun getApiKeysShouldReturnListWhenAuthenticated() {
        `when`(apiKeyService.listApiKeys()).thenReturn(listOf(givenApiKey()))

        mockMvc.perform(get("/admin/api-keys").with(httpBasic("admin", "test-admin-password")))
            .andExpect(status().isOk)
            .andExpect(view().name("admin/api-keys"))
            .andExpect(model().attributeExists("apiKeys"))
            .andExpect(content().string(containsString("name=\"_csrf\"")))
    }

    @Test
    fun postApiKeysShouldBeRejectedWithoutCsrfToken() {
        mockMvc.perform(
            post("/admin/api-keys")
                .with(httpBasic("admin", "test-admin-password"))
                .param("name", "Maeva App")
                .param("namespace", "maeva-app-1")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun postApiKeysShouldCreateKeyAndRedirectWithFlashAttributes() {
        val apiKey = givenApiKey()
        `when`(apiKeyService.createApiKey("Maeva App", "maeva-app-1"))
            .thenReturn(ApiKeyCreationResult(apiKey = apiKey, rawKey = "key123.s3cr3t"))

        mockMvc.perform(
            post("/admin/api-keys")
                .with(csrf())
                .with(httpBasic("admin", "test-admin-password"))
                .param("name", "Maeva App")
                .param("namespace", "maeva-app-1")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/admin/api-keys"))
            .andExpect(flash().attribute("newRawKey", "key123.s3cr3t"))
            .andExpect(flash().attribute("newKeyName", "Maeva App"))
    }

    @Test
    fun postApiKeysShouldRequireAuthentication() {
        mockMvc.perform(
            post("/admin/api-keys")
                .with(csrf())
                .param("name", "Maeva App")
                .param("namespace", "maeva-app-1")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun toggleApiKeyShouldDisableAndRedirect() {
        doAnswer { givenApiKey().apply { isEnabled = false } }
            .`when`(apiKeyService).setEnabled(eq("id1"), eq(false))

        mockMvc.perform(
            post("/admin/api-keys/id1/toggle")
                .with(csrf())
                .with(httpBasic("admin", "test-admin-password"))
                .param("isEnabled", "false")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/admin/api-keys"))
    }

    @Test
    fun toggleApiKeyShouldRequireAuthentication() {
        mockMvc.perform(
            post("/admin/api-keys/id1/toggle")
                .with(csrf())
                .param("isEnabled", "false")
        )
            .andExpect(status().isUnauthorized)
    }
}
