package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.enums.ViewNameEnum
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView

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
        private val viewNameEnum = ViewNameEnum.ERROR
    }
}
