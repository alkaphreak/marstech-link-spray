package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.controller.api.ShortenerApiController
import fr.marstech.mtlinkspray.enums.ViewNameEnum.SHORTENER
import fr.marstech.mtlinkspray.service.ShortenerService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Validated
@Controller
@RequestMapping("/shortener")
open class ViewShortenerController(
    val shortenerApiController: ShortenerApiController,
    val shortenerService: ShortenerService
) : ThymeleafViewControllerInterface {

    override fun getModelAndView() = getModelAndView(SHORTENER)

    @GetMapping("/shortener")
    fun getShortenerPage(): ModelAndView = getModelAndView()

    @PostMapping("/shortener")
    fun createShortenedLink(
        @RequestParam(name = "input-link") @NotBlank inputLink: String,
        httpServletRequest: HttpServletRequest
    ): ModelAndView = getModelAndView()
        .addObject("inputLink", inputLink)
        .addObject("shortenedLink", shortenerApiController.getShort(inputLink, httpServletRequest))

    /**
     * Forward a short link to the real link
     *
     * @param shortUrlUid the short code of the url
     * @param httpServletRequest the httpServletRequest
     * @return a [ModelAndView] with redirect to the target url
     */
    @GetMapping("/{shortUrlUid}")
    fun getTarget(
        @PathVariable(name = "shortUrlUid") @NotBlank shortUrlUid: String,
        httpServletRequest: HttpServletRequest,
    ): ModelAndView = getModelAndViewToRedirect(shortenerService.getTarget(shortUrlUid, httpServletRequest))
}
