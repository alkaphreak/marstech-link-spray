package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.controller.api.ShortenerApiController
import fr.marstech.mtlinkspray.enums.ViewNameEnum.SHORTENER
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Validated
@Controller
@RequestMapping("/shortener")
open class ViewShortenerController(val shortenerApiController: ShortenerApiController) :
    ThymeleafViewControllerInterface {

    override fun getModelAndView() = getModelAndView(SHORTENER)

    @GetMapping
    fun getShortenerPage(): ModelAndView = getModelAndView()

    @PostMapping
    fun createShortenedLink(
        @RequestParam(name = "input-link") inputLink: @NotBlank String?,
        httpServletRequest: HttpServletRequest?
    ): ModelAndView = getModelAndView()
        .addObject("inputLink", inputLink)
        .addObject("shortenedLink", shortenerApiController.getShort(inputLink, httpServletRequest))
}
