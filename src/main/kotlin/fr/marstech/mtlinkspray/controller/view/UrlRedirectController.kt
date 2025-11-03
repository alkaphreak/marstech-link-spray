package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.service.ShortenerService
import jakarta.validation.constraints.NotBlank
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
        @PathVariable(name = "shortUrlUid") @NotBlank shortUrlUid: String,
    ): ModelAndView = getModelAndViewToRedirect(shortenerService.getTarget(shortUrlUid))

    override fun getModelAndView(): ModelAndView? {
        throw NotImplementedError("This method is not implemented")
    }
}