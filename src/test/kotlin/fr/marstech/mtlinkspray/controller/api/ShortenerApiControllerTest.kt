package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.ShortenerService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ShortenerApiControllerTest {

    private lateinit var mockMvc: MockMvc
    private val shortenerService: ShortenerService = mock()

    @BeforeEach
    fun setup() {
        val validator = org.springframework.validation.beanvalidation.LocalValidatorFactoryBean()
        validator.afterPropertiesSet()
        
        mockMvc = MockMvcBuilders.standaloneSetup(ShortenerApiController(shortenerService))
            .setControllerAdvice(fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler())
            .setValidator(validator)
            .build()
    }

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
    fun shouldHandleServiceException() {
        whenever(shortenerService.shorten(any<String>(), any()))
            .thenThrow(IllegalArgumentException("Service error"))

        mockMvc.perform(get("/api/url-shortener/shorten").param("url", "https://example.com"))
            .andExpect(status().isBadRequest)
    }
}