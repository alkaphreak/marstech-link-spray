package fr.marstech.mtlinkspray.adapter.inbound.web.common
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.ERROR
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView

@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice(annotations = [Controller::class])
class GlobalExceptionHandler : ThymeleafViewControllerInterface {
    private val log = LoggerFactory.getLogger(javaClass)

    // The page never shows the exception message: it can expose class internals (MLS-282)
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ModelAndView {
        log.error("Unhandled error in a view", e)
        return getModelAndView().addObject("errorMessage", "An unexpected error occurred")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ModelAndView =
        getModelAndView().addObject("errorMessage", e.message)

    override fun getModelAndView(): ModelAndView = getModelAndView(viewNameEnum)

    companion object {
        private val viewNameEnum = ERROR
    }
}