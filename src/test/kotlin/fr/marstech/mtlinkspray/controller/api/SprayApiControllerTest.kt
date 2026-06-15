package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.SprayService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SprayApiController::class)
class SprayApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var sprayService: SprayService

    @Test
    fun getSprayReturnsExpectedResult() {

        val respectedResponse = "http://localhost/spray/open?spray=https://example.com&spray=https://example.org"

        get("/api/spray")
            .param("inputLinkList", "https://example.com", "https://example.org")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(
                content()
                    .string(respectedResponse)
            )
    }

    @Test
    fun shouldHandleSingleUrl() {
        val expectedResponse = "http://localhost/spray/open?spray=https://example.com"

        get("/api/spray")
            .param("inputLinkList", "https://example.com")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().string(expectedResponse))
    }

    @Test
    fun shouldReturn400ForEmptyLinkList() {
        get("/api/spray")
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturn400ForBlankLinks() {
        get("/api/spray")
            .param("inputLinkList", "", "   ")
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldHandleMultipleUrls() {
        val expectedResponse = "http://localhost/spray/open?spray=https://a.com&spray=https://b.com&spray=https://c.com"

        get("/api/spray")
            .param("inputLinkList", "https://a.com", "https://b.com", "https://c.com")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().string(expectedResponse))
    }

    @Test
    fun shouldReturnShortenedSprayLinkWhenShortenIsTrue() {
        // Given
        val expectedShortUrl = "http://localhost/xyz789"
        whenever(sprayService.shortenAndSpray(any(), any())).thenReturn(expectedShortUrl)

        // When / Then
        get("/api/spray")
            .param("inputLinkList", "https://example.com", "https://example.org")
            .param("shorten", "true")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().string(expectedShortUrl))
    }

    @Test
    fun shouldReturnRawSprayLinkWhenShortenIsFalse() {
        val expectedResponse = "http://localhost/spray/open?spray=https://example.com&spray=https://example.org"

        get("/api/spray")
            .param("inputLinkList", "https://example.com", "https://example.org")
            .param("shorten", "false")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().string(expectedResponse))
    }
}