package fr.marstech.mtlinkspray.controller.view


import fr.marstech.mtlinkspray.entity.WriteSignatureEntity.ResourceType
import fr.marstech.mtlinkspray.enums.ViewNameEnum.SHORTENER
import fr.marstech.mtlinkspray.service.ShortenerService
import fr.marstech.mtlinkspray.service.WriteSignatureService
import fr.marstech.mtlinkspray.validation.ValidUrl
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/shortener")
class ViewShortenerController(
    val shortenerService: ShortenerService,
    private val writeSignatureService: WriteSignatureService,
) : ThymeleafViewControllerInterface {

    override fun getModelAndView() = getModelAndView(SHORTENER)

    @GetMapping
    fun getShortenerPage(): ModelAndView = getModelAndView()

    @PostMapping
    fun createShortenedLink(
        @RequestParam(name = "input-link") @NotBlank @ValidUrl inputLink: String,
        @RequestParam(required = false) fp: String?,
        httpServletRequest: HttpServletRequest
    ): ModelAndView {
        val shortenedLink = shortenerService.shorten(inputLink, httpServletRequest)
        writeSignatureService.record(ResourceType.SHORT_LINK, shortenedLink, fp, httpServletRequest)
        return getModelAndView()
            .addObject("inputLink", inputLink)
            .addObject("shortenedLink", shortenedLink)
    }
}
