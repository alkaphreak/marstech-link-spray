package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.view.ThymeleafViewControllerInterface
import fr.marstech.mtlinkspray.enums.ViewNameEnum
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.web.servlet.ModelAndView

class ThymeleafViewControllerInterfaceTest {

    // Provide a simple implementation for testing
    private val controller = object : ThymeleafViewControllerInterface {
        override fun getModelAndView(): ModelAndView? = null
        override fun getModelAndView(viewNameEnum: ViewNameEnum): ModelAndView {
            val mav = ModelAndView(viewNameEnum.viewName)
            mav.addObject("viewNameEnum", viewNameEnum)
            return mav
        }
        override fun getModelAndView(viewNameEnum: ViewNameEnum, httpServletRequest: HttpServletRequest): ModelAndView {
            val mav = ModelAndView(viewNameEnum.viewName)
            mav.addObject("viewNameEnum", viewNameEnum)
            mav.addObject("headers", mapOf<String, String>())
            return mav
        }
    }

    @Test
    fun `getModelAndView should return ModelAndView with correct view name and attribute`() {
        val viewNameEnum = ViewNameEnum.ABUSE
        val result = controller.getModelAndView(viewNameEnum)
        assertEquals(viewNameEnum.viewName, result.viewName)
        assertEquals(viewNameEnum, result.model["viewNameEnum"])
    }

    @Test
    fun `getModelAndView with request should add headers`() {
        val viewNameEnum = ViewNameEnum.ABUSE
        val request = mock(HttpServletRequest::class.java)
        val result = controller.getModelAndView(viewNameEnum, request)
        assertEquals(viewNameEnum.viewName, result.viewName)
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

    // No Mockito verification or matcher usage in this test class. No unfinished verification present.
}