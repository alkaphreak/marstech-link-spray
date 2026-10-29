package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.conf.ValidationConfig
import fr.marstech.mtlinkspray.controller.api.RootApiController
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.controller.view.ViewShortenerController
import fr.marstech.mtlinkspray.service.ShortenerService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// MLS-59: the web form must enforce the same URL rules (length, protocol) as the API
// RootApiController: the page layout reads @rootApiController.version
@WebMvcTest(controllers = [ViewShortenerController::class, RootApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class, ValidationConfig::class)
class ViewShortenerControllerMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var shortenerService: ShortenerService

    @Test
    fun shouldShortenValidUrl() {
        // Given
        whenever(shortenerService.shorten(any<String>(), any())).thenReturn("https://short.ly/abc123")

        // When / Then
        mockMvc.perform(post("/shortener").param(INPUT_LINK, "https://example.com"))
            .andExpect(status().isOk)
    }

    @Test
    fun shouldRejectUrlLongerThan2048Characters() {
        // Given
        val tooLong = "https://example.com/" + "a".repeat(2049)

        // When / Then
        mockMvc.perform(post("/shortener").param(INPUT_LINK, tooLong))
            .andExpect(status().isBadRequest)
        verify(shortenerService, never()).shorten(any<String>(), any())
    }

    @Test
    fun shouldRejectNonHttpProtocol() {
        // When / Then
        mockMvc.perform(post("/shortener").param(INPUT_LINK, "ftp://example.com/file"))
            .andExpect(status().isBadRequest)
        verify(shortenerService, never()).shorten(any<String>(), any())
    }

    private companion object {
        const val INPUT_LINK = "input-link"
    }
}
