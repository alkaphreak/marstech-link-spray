package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.conf.ValidationConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.service.ShortenerService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// Full MVC slice with ValidationConfig, so @Validated runs through the same proxy as in production
@WebMvcTest(controllers = [ShortenerApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class, ValidationConfig::class)
class ShortenerApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var shortenerService: ShortenerService

    @Test
    fun shouldGetShortenedUrl() {
        val inputUrl = "https://example.com"
        val shortenedUrl = "https://short.ly/abc123"

        whenever(shortenerService.shorten(any<String>(), any())).thenReturn(shortenedUrl)

        mockMvc.perform(get("/api/url-shortener/shorten").param("url", inputUrl))
            .andExpect(status().isOk)
            .andExpect(content().string(shortenedUrl))
    }

    @Test
    fun shouldReturn400ForMissingUrl() {
        mockMvc.perform(get("/api/url-shortener/shorten"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturn400ForBlankUrl() {
        mockMvc.perform(get("/api/url-shortener/shorten").param("url", ""))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturn400ForWhitespaceUrl() {
        mockMvc.perform(get("/api/url-shortener/shorten").param("url", "   "))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturn400WithFieldErrorForInvalidUrl() {
        mockMvc.perform(get("/api/url-shortener/shorten").param("url", "notaurl"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors[0].field").value("url"))
            .andExpect(jsonPath("$.fieldErrors[0].message").value("URL must have a protocol (e.g., http:// or https://)"))
    }

    @Test
    fun shouldHandleServiceException() {
        whenever(shortenerService.shorten(any<String>(), any()))
            .thenThrow(IllegalArgumentException("Service error"))

        mockMvc.perform(get("/api/url-shortener/shorten").param("url", "https://example.com"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturn500WhenServiceReturnsNull() {
        // Given
        val inputUrl = "https://example.com"
        whenever(shortenerService.shorten(any<String>(), any())).thenReturn(null)

        // When / Then
        mockMvc.perform(get("/api/url-shortener/shorten").param("url", inputUrl))
            .andExpect(status().isInternalServerError)
    }
}