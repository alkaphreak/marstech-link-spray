package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum
import fr.marstech.mtlinkspray.utils.NetworkUtils.getHeadersAsMap
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.ModelAndView

/**
 * Interface for controllers returning Thymeleaf views.
 */
interface ThymeleafViewControllerInterface {

    fun getModelAndView(): ModelAndView?

    /**
     * Returns a ModelAndView for the given view name.
     */
    fun getModelAndView(viewNameEnum: ViewNameEnum): ModelAndView =
        ModelAndView(viewNameEnum.viewName).addObject("viewNameEnum", viewNameEnum)

    /**
     * Returns a ModelAndView for the given view name and request headers.
     */
    fun getModelAndView(viewNameEnum: ViewNameEnum, httpServletRequest: HttpServletRequest): ModelAndView =
        ModelAndView(viewNameEnum.viewName)
            .addObject("viewNameEnum", viewNameEnum)
            .addObject("headers", getHeadersAsMap(httpServletRequest))

    /**
     * Returns a ModelAndView to forward to the given URL.
     */
    fun getModelAndViewToForward(forwardUrl: String): ModelAndView =
        ModelAndView("forward:$forwardUrl")

    /**
     * Returns a ModelAndView to redirect to the given URL.
     */
    fun getModelAndViewToRedirect(redirectUrl: String): ModelAndView =
        ModelAndView(getRedirectUrl(redirectUrl))

    /**
     * Returns a redirect URL string.
     */
    fun getRedirectUrl(url: String): String = "redirect:$url"

}