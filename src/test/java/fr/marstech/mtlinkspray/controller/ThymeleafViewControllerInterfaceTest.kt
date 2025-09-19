package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.enums.ViewNameEnum
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.web.servlet.ModelAndView

class ThymeleafViewControllerInterfaceTest {

    // Minimal implementation for testing
    private val controller = object : ThymeleafViewControllerInterface {
        override fun getModelAndView(): ModelAndView? = null
    }

    @Test
    fun `getModelAndView should return ModelAndView with correct view name and attribute`() {
        val viewNameEnum = mock(ViewNameEnum::class.java)
        `when`(viewNameEnum.viewName).thenReturn("testView")

        val result = controller.getModelAndView(viewNameEnum)
        assertEquals("testView", result.viewName)
        assertEquals(viewNameEnum, result.model["viewNameEnum"])
    }

    @Test
    fun `getModelAndView with request should add headers`() {
        val viewNameEnum = mock(ViewNameEnum::class.java)
        `when`(viewNameEnum.viewName).thenReturn("testView")
        val request = mock(HttpServletRequest::class.java)

        val result = controller.getModelAndView(viewNameEnum, request)
        assertEquals("testView", result.viewName)
        assertEquals(viewNameEnum, result.model["viewNameEnum"])
        assertTrue(result.model.containsKey("headers"))
    }

    @Test
    fun `getModelAndViewToForward should return ModelAndView with forward url`() {
        val result = controller.getModelAndViewToForward("/forwardUrl")
        assertEquals("forward:/forwardUrl", result.viewName)
    }

    @Test
    fun `getRedirectUrl should return redirect string`() {
        val result = controller.getRedirectUrl("/redirectUrl")
        assertEquals("redirect:/redirectUrl", result)
    }
}