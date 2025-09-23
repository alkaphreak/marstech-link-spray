package fr.marstech.mtlinkspray.controller.commons

import fr.marstech.mtlinkspray.controller.view.ThymeleafViewControllerInterface
import fr.marstech.mtlinkspray.enums.ViewNameEnum.ERROR
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView

@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice(annotations = [Controller::class])
class GlobalExceptionHandler : ThymeleafViewControllerInterface {

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ModelAndView =
        getModelAndView().addObject("errorMessage", e.message)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ModelAndView =
        getModelAndView().addObject("errorMessage", e.message)

    override fun getModelAndView(): ModelAndView = getModelAndView(viewNameEnum)

    companion object {
        private val viewNameEnum = ERROR
    }
}