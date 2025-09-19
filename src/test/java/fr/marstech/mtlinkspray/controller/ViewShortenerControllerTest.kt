package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.api.ApiShortenerController
import fr.marstech.mtlinkspray.enums.ViewNameEnum
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class ViewShortenerControllerTest {

    private val apiShortenerController = mock(ApiShortenerController::class.java)
    private val httpServletRequest = mock(HttpServletRequest::class.java)
    private val controller = ViewShortenerController(apiShortenerController)

    @Test
    fun `getView should return ModelAndView with correct view name`() {
        val modelAndView = controller.getView()
        assertEquals(ViewNameEnum.SHORTENER.viewName, modelAndView.viewName)
    }

    @Test
    fun `postView should add inputLink and shortenedLink to model`() {
        val inputLink = "https://example.com"
        val shortenedLink = "https://short.ly/abc123"
        `when`(apiShortenerController.getShort(inputLink, httpServletRequest)).thenReturn(shortenedLink)

        val modelAndView = controller.postView(inputLink, httpServletRequest)
        assertEquals(inputLink, modelAndView.model["inputLink"])
        assertEquals(shortenedLink, modelAndView.model["shortenedLink"])
        assertEquals(ViewNameEnum.SHORTENER.viewName, modelAndView.viewName)
    }
}