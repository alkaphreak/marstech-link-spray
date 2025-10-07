package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.view.ViewSprayController
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ViewSprayControllerTest {

    private val controller = ViewSprayController()

    @Test
    fun `GET spray page returns correct view`() {
        val request = MockHttpServletRequest()
        val result: ModelAndView = controller.getSprayPage(request)
        
        assertEquals("spray", result.viewName)
        assertNotNull(result.model["headers"])
    }

    @Test
    fun `POST spray returns correct view with model`() {
        val request = MockHttpServletRequest()
        val result: ModelAndView = controller.getLink("https://example.com", request)
        
        assertEquals("spray", result.viewName)
        assertNotNull(result.model["linkList"])
        assertNotNull(result.model["linkListText"])
        assertNotNull(result.model["linkSpray"])
    }

    @Test
    fun `GET spray open returns correct view with model`() {
        val sprayList = listOf("https://example.com")
        val result: ModelAndView = controller.getSpray(sprayList)
        
        assertEquals("spray", result.viewName)
        assertNotNull(result.model["linkListText"])
        assertNotNull(result.model["spray"])
    }
}