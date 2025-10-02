package fr.marstech.mtlinkspray.controller.view


import fr.marstech.mtlinkspray.enums.ViewNameEnum.SHORTENER
import fr.marstech.mtlinkspray.service.ShortenerService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView

@Validated
@Controller
@RequestMapping("/shortener")
open class ViewShortenerController(
    val shortenerService: ShortenerService
) : ThymeleafViewControllerInterface {

    override fun getModelAndView() = getModelAndView(SHORTENER)

    @GetMapping
    fun getShortenerPage(): ModelAndView = getModelAndView()

    @PostMapping
    fun createShortenedLink(
        @RequestParam(name = "input-link") @NotBlank inputLink: String, httpServletRequest: HttpServletRequest
    ): ModelAndView = getModelAndView()
        .addObject("inputLink", inputLink)
        .addObject("shortenedLink", shortenerService.shorten(inputLink, httpServletRequest))

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
}
