package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.service.ShortenerService
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.servlet.ModelAndView

@Controller
class UrlRedirectController(val shortenerService: ShortenerService) : ThymeleafViewControllerInterface {

    /**
     * Forward a short link to the real link
     *
     * @param shortUrlUid the short code of the url
     * @return a [ModelAndView] with redirect to the target url
     */
    @GetMapping("/{shortUrlUid}")
    fun getTarget(
        @PathVariable @NotBlank shortUrlUid: String,
        @RequestHeader(HttpHeaders.USER_AGENT, required = false) userAgent: String?,
        method: HttpMethod,
    ): ModelAndView = getModelAndViewToRedirect(
        // A HEAD is a link check, never a person reading: no User-Agent makes it a robot's visit (MLS-273)
        shortenerService.getTarget(shortUrlUid, userAgent.takeUnless { method == HttpMethod.HEAD })
    )

    override fun getModelAndView(): ModelAndView? {
        throw NotImplementedError("This method is not implemented")
    }
}