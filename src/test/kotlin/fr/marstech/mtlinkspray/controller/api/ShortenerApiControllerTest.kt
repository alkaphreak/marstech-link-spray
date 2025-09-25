package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.ShortenerService
import fr.marstech.mtlinkspray.service.ShortenerServiceImpl
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.URI

@WebMvcTest
class ShortenerApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var shortenerService: ShortenerService

    @Test
    fun shouldGetShortenedUrl() {
        val inputUrl = "https://example.com"
        val shortenedUrl = "https://short.ly/abc123"

        `when`(
            shortenerService.shorten(
                ArgumentMatchers.eq(inputUrl),
                ArgumentMatchers.any(HttpServletRequest::class.java)
            )
        ).thenReturn(shortenedUrl)

        get("/api/url-shortener/shorten")
            .param("url", inputUrl)
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().string(shortenedUrl))
    }
}
