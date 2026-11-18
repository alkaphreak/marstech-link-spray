package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.conf.ValidationConfig
import fr.marstech.mtlinkspray.controller.api.RootApiController
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.service.ShortenerService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

private const val UNKNOWN_UID = "nope-404"
private const val BROWSER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"

// MLS-274: an unknown short link is a 404 page for a browser and a 404 JSON body for an API client
@WebMvcTest(controllers = [UrlRedirectController::class, RootApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class, ValidationConfig::class)
class UrlRedirectControllerMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var shortenerService: ShortenerService

    @BeforeEach
    fun setUp() {
        whenever(shortenerService.getTarget(UNKNOWN_UID))
            .thenThrow(NoSuchElementException("Short link '$UNKNOWN_UID' does not exist"))
    }

    @Test
    fun unknownShortLinkFromBrowserRendersErrorPageWith404() {
        mockMvc.perform(
            get("/$UNKNOWN_UID")
                .header("Accept", BROWSER_ACCEPT)
        )
            .andExpect(status().isNotFound)
            .andExpect(view().name("error"))
            .andExpect(content().contentTypeCompatibleWith("text/html"))
    }

    @Test
    fun unknownShortLinkFromHtmlOnlyClientIs404NotServerError() {
        mockMvc.perform(get("/$UNKNOWN_UID").header("Accept", "text/html"))
            .andExpect(status().isNotFound)
            .andExpect(view().name("error"))
    }

    @Test
    fun unknownShortLinkWithoutAcceptKeepsJson404() {
        mockMvc.perform(get("/$UNKNOWN_UID"))
            .andExpect(status().isNotFound)
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
    }

    @Test
    fun unknownMultiSegmentPathFromBrowserRendersErrorPageWith404() {
        mockMvc.perform(get("/nope/404").header("Accept", BROWSER_ACCEPT))
            .andExpect(status().isNotFound)
            .andExpect(view().name("error"))
    }

    @Test
    fun unknownShortLinkFromApiClientKeepsJson404() {
        mockMvc.perform(get("/$UNKNOWN_UID").accept(APPLICATION_JSON))
            .andExpect(status().isNotFound)
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Short link '$UNKNOWN_UID' does not exist"))
    }
}
