package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.ShortenerService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Disabled
@ExtendWith(SpringExtension::class)
@WebMvcTest(ShortenerApiController::class)
class ShortenerApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var shortenerService: ShortenerService

    @Test
    fun shouldGetShortenedUrl() {
        val inputUrl = "https://example.com"
        val shortenedUrl = "https://short.ly/abc123"

        whenever(shortenerService.shorten(any(), any())).thenReturn(shortenedUrl)

        mockMvc.perform(get("/api/url-shortener/shorten").param("url", inputUrl))
            .andExpect(status().isOk)
            .andExpect(content().string(shortenedUrl))
    }
}
