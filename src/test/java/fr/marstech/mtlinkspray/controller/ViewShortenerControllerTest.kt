package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.api.ShortenerApiController
import fr.marstech.mtlinkspray.controller.view.ViewShortenerController
import fr.marstech.mtlinkspray.enums.ViewNameEnum
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ViewShortenerControllerTest {

    private val shortenerApiController = mock(ShortenerApiController::class.java)
    private val httpServletRequest = mock(HttpServletRequest::class.java)
    private val controller = ViewShortenerController(shortenerApiController)

    @Test
    fun `getView should return ModelAndView with correct view name`() {
        val modelAndView = controller.getModelAndView()
        assertEquals(ViewNameEnum.SHORTENER.viewName, modelAndView.viewName)
    }

    @Test
    fun `postView should add inputLink and shortenedLink to model`() {
        val inputLink = "https://example.com"
        val shortenedLink = "https://short.ly/abc123"
        `when`(shortenerApiController.getShort(inputLink, httpServletRequest)).thenReturn(shortenedLink)

        val modelAndView = controller.createShortenedLink(inputLink, httpServletRequest)
        assertEquals(inputLink, modelAndView.model["inputLink"])
        assertEquals(shortenedLink, modelAndView.model["shortenedLink"])
        assertEquals(ViewNameEnum.SHORTENER.viewName, modelAndView.viewName)
    }
}