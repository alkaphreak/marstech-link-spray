package fr.marstech.mtlinkspray.controller.commons

import fr.marstech.mtlinkspray.enums.ViewNameEnum.ERROR
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.web.servlet.ModelAndView

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun handleExceptionShouldAddErrorMessageToModel() {
        val ex = Exception("Something went wrong")
        val mav: ModelAndView = handler.handleException(ex)
        assertEquals(ERROR.viewName, mav.viewName)
        assertEquals("Something went wrong", mav.model["errorMessage"])
    }

    @Test
    fun handleIllegalArgumentShouldAddErrorMessageToModel() {
        val ex = IllegalArgumentException("Invalid argument")
        val mav: ModelAndView = handler.handleIllegalArgument(ex)
        assertEquals(ERROR.viewName, mav.viewName)
        assertEquals("Invalid argument", mav.model["errorMessage"])
    }
}