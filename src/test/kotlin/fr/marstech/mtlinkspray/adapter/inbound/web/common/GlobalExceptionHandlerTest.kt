package fr.marstech.mtlinkspray.adapter.inbound.web.common
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.ERROR
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.web.servlet.ModelAndView

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun handleExceptionShouldShowAGenericMessage() {
        val ex = Exception("Failed to instantiate fr.marstech.Secret using constructor ...")
        val mav: ModelAndView = handler.handleException(ex)
        assertEquals(ERROR.viewName, mav.viewName)
        assertEquals("An unexpected error occurred", mav.model["errorMessage"])
    }

    @Test
    fun handleIllegalArgumentShouldAddErrorMessageToModel() {
        val ex = IllegalArgumentException("Invalid argument")
        val mav: ModelAndView = handler.handleIllegalArgument(ex)
        assertEquals(ERROR.viewName, mav.viewName)
        assertEquals("Invalid argument", mav.model["errorMessage"])
    }
}