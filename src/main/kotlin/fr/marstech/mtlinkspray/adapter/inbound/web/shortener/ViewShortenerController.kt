package fr.marstech.mtlinkspray.adapter.inbound.web.shortener
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ThymeleafViewControllerInterface
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.clientContext
import fr.marstech.mtlinkspray.domain.signature.ResourceType
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.SHORTENER
import fr.marstech.mtlinkspray.domain.shortener.ShortenerService
import fr.marstech.mtlinkspray.domain.signature.WriteSignatureService
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ValidUrl
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
        val shortenedLink = shortenerService.shorten(inputLink, RequestLinkUrls(httpServletRequest))
        writeSignatureService.record(ResourceType.SHORT_LINK, shortenedLink, fp, clientContext(httpServletRequest))
        return getModelAndView()
            .addObject("inputLink", inputLink)
            .addObject("shortenedLink", shortenedLink)
    }
}
