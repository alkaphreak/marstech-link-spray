package fr.marstech.mtlinkspray.adapter.inbound.web.shortener
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.SHORTENER
import fr.marstech.mtlinkspray.domain.shortener.ShortenerService
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ViewShortenerControllerTest {

    private val shortenerService = mock(ShortenerService::class.java)
    private val httpServletRequest = mock(HttpServletRequest::class.java)

    private val controller = ViewShortenerController(shortenerService, mock(fr.marstech.mtlinkspray.domain.signature.WriteSignatureService::class.java))

    @Test
    fun getViewShouldReturnModelAndViewWithCorrectViewName() {
        val modelAndView = controller.getModelAndView()
        assertEquals(SHORTENER.viewName, modelAndView.viewName)
    }

    @Test
    fun postViewShouldAddInputLinkAndShortenedLinkToModel() {
        // Given
        val inputLink = "https://example.com"
        val shortenedLink = "https://short.ly/abc123"
        `when`(shortenerService.shorten(org.mockito.kotlin.eq(inputLink), org.mockito.kotlin.any())).thenReturn(shortenedLink)

        // When
        val modelAndView = controller.createShortenedLink(inputLink, null, httpServletRequest)

        // Then
        assertEquals(inputLink, modelAndView.model["inputLink"])
        assertEquals(shortenedLink, modelAndView.model["shortenedLink"])
        assertEquals(SHORTENER.viewName, modelAndView.viewName)
    }

    @Test
    fun postViewShouldReturnViewWithoutShortenedLinkWhenInputIsBlank() {
        // Given
        val blankInput = "   "
        `when`(shortenerService.shorten(org.mockito.kotlin.eq(blankInput), org.mockito.kotlin.any())).thenReturn(null)

        // When
        val modelAndView = controller.createShortenedLink(blankInput, null, httpServletRequest)

        // Then
        assertEquals(SHORTENER.viewName, modelAndView.viewName)
        assertNull(modelAndView.model["shortenedLink"])
    }
}