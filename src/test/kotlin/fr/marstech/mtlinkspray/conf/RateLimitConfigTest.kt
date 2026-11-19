package fr.marstech.mtlinkspray.conf

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.controller.api.PasteApiController
import fr.marstech.mtlinkspray.controller.api.RootApiController
import fr.marstech.mtlinkspray.controller.api.ShortenerApiController
import fr.marstech.mtlinkspray.controller.view.ViewAbuseController
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.service.AnonymousQuota
import fr.marstech.mtlinkspray.service.PasteService
import fr.marstech.mtlinkspray.service.ReportAbuseService
import fr.marstech.mtlinkspray.service.ShortenerService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders.RETRY_AFTER
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@WebMvcTest(controllers = [PasteApiController::class, RootApiController::class, ShortenerApiController::class, ViewAbuseController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
@TestPropertySource(
    properties = [
        "mt.link-spray.rate-limit.writes.quota=1",
        "mt.link-spray.rate-limit.writes.window-ms=600000",
        "mt.link-spray.rate-limit.abuse.quota=1",
        "mt.link-spray.rate-limit.abuse.window-ms=3600000",
    ]
)
class RateLimitConfigTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var pasteService: PasteService

    @MockitoBean
    lateinit var shortenerService: ShortenerService

    @MockitoBean
    lateinit var reportAbuseService: ReportAbuseService

    private val paste = """{"title":"t","content":"c","language":"KOTLIN","expiration":"1h","isPrivate":false}"""

    private fun postPaste(ip: String) = post("/api/paste")
        .header("CF-Connecting-IP", ip)
        .contentType(APPLICATION_JSON)
        .content(paste)

    @Test
    fun shouldReturnTooManyRequestsWithRetryAfterOnceWriteQuotaIsSpent() {
        // Given: the first paste from this IP spends its only token (its own status is irrelevant here)
        mockMvc.perform(postPaste("203.0.113.10"))

        // When / Then
        mockMvc.perform(postPaste("203.0.113.10"))
            .andExpect(status().isTooManyRequests)
            .andExpect(header().exists(RETRY_AFTER))
            .andExpect(jsonPath("$.message").value("Write quota of 1 reached: retry later"))
    }

    @Test
    fun shouldNotLimitReadsOnALimitedPath() {
        // Given
        mockMvc.perform(postPaste("203.0.113.11"))

        // When / Then: a GET on /api/paste/{id} is not a write route
        mockMvc.perform(get("/api/paste/abc").header("CF-Connecting-IP", "203.0.113.11"))
            .andExpect { check(it.response.status != 429) { "GET was rate limited" } }
    }

    @Test
    fun shouldIgnoreRequestsWithAnotherMethod() {
        // Given: an interceptor for POST only, with a quota that would throw on the first count
        val quota = mock<AnonymousQuota>()
        val interceptor = RateLimitInterceptor(quota, setOf("POST"))
        val request = MockHttpServletRequest("GET", "/abuse")

        // When
        interceptor.preHandle(request, MockHttpServletResponse(), Any())

        // Then
        verify(quota, never()).count(org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }

    @Test
    fun shouldCountHeadLikeGetOnTheShortenRoute() {
        // Given: Spring MVC sends HEAD to the GET handler, which creates the link
        mockMvc.perform(head("/api/url-shortener/shorten").param("url", "https://example.com").header("CF-Connecting-IP", "203.0.113.12"))

        // When / Then
        mockMvc.perform(get("/api/url-shortener/shorten").param("url", "https://example.com").header("CF-Connecting-IP", "203.0.113.12"))
            .andExpect(status().isTooManyRequests)
    }

    @Test
    fun shouldRenderErrorPageWithRetryAfterWhenAFormIsRateLimited() {
        // Given
        val abuse = post("/abuse").param("inputAbuseDecsription", "spam").accept(TEXT_HTML).header("CF-Connecting-IP", "203.0.113.13")
        mockMvc.perform(abuse)

        // When / Then
        mockMvc.perform(abuse)
            .andExpect(status().isTooManyRequests)
            .andExpect(view().name("error"))
            .andExpect(header().exists(RETRY_AFTER))
    }
}
