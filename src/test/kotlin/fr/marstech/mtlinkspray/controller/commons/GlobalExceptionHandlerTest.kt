package fr.marstech.mtlinkspray.controller.commons

import fr.marstech.mtlinkspray.enums.ViewNameEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.web.servlet.ModelAndView

@Disabled
class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleException should add errorMessage to model`() {
        val ex = Exception("Something went wrong")
        val mav: ModelAndView = handler.handleException(ex)
        assertEquals(ViewNameEnum.ERROR.viewName, mav.viewName)
        assertEquals("Something went wrong", mav.model["errorMessage"])
    }

    @Test
    fun `handleIllegalArgument should add errorMessage to model`() {
        val ex = IllegalArgumentException("Invalid argument")
        val mav: ModelAndView = handler.handleIllegalArgument(ex)
        assertEquals(ViewNameEnum.ERROR.viewName, mav.viewName)
        assertEquals("Invalid argument", mav.model["errorMessage"])
    }
}