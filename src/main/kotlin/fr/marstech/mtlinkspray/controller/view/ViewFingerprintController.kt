package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.FINGERPRINT
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

/** Shows visitors what their browser gives away (MLS-254). Nothing is stored. */
@Controller
@RequestMapping("/fingerprint")
class ViewFingerprintController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(FINGERPRINT)

    // The server-side half of the fingerprint: headers the browser sends with every request.
    @GetMapping
    fun home(
        @RequestHeader(HttpHeaders.USER_AGENT, required = false) userAgent: String?,
        @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE, required = false) acceptLanguage: String?
    ): ModelAndView = getModelAndView()
        .addObject("userAgent", userAgent)
        .addObject("acceptLanguage", acceptLanguage)
}
