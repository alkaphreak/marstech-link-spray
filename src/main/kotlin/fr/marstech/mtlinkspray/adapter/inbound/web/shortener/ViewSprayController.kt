package fr.marstech.mtlinkspray.adapter.inbound.web.shortener
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ThymeleafViewControllerInterface
import fr.marstech.mtlinkspray.adapter.outbound.mongo.signature.WriteSignatureEntity.ResourceType
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.SPRAY
import fr.marstech.mtlinkspray.domain.shortener.SprayService
import fr.marstech.mtlinkspray.domain.signature.WriteSignatureService
import fr.marstech.mtlinkspray.domain.shortener.SprayService.Companion.getLinkList
import fr.marstech.mtlinkspray.domain.shortener.SprayService.Companion.getLinkListText
import fr.marstech.mtlinkspray.domain.shortener.SprayService.Companion.getLinkSpray
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.getHeadersAsMap
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/spray")
class ViewSprayController(
    private val sprayService: SprayService,
    private val writeSignatureService: WriteSignatureService,
) : ThymeleafViewControllerInterface {

    @GetMapping
    fun getSprayPage(httpServletRequest: HttpServletRequest): ModelAndView =
        getModelAndView().addObject("headers", getHeadersAsMap(httpServletRequest))

    @PostMapping
    fun getLink(
        @RequestParam inputLinkList: String, httpServletRequest: HttpServletRequest
    ): ModelAndView = getLinkList(inputLinkList).let { linkList ->
        getModelAndView().addObject("linkList", linkList)
            .addObject("linkListText", getLinkListText(linkList))
            .addObject("linkSpray", getLinkSpray(httpServletRequest, linkList))
    }

    @PostMapping("/shorten")
    fun getShortenedSpray(
        @RequestParam inputLinkList: String,
        @RequestParam(required = false) fp: String?,
        httpServletRequest: HttpServletRequest
    ): ModelAndView {
        val linkList = getLinkList(inputLinkList)
        val shortenedSprayUrl = sprayService.shortenAndSpray(linkList, httpServletRequest) { child ->
            writeSignatureService.record(ResourceType.SHORT_LINK, child, fp, httpServletRequest)
        }
        writeSignatureService.record(ResourceType.SPRAY, shortenedSprayUrl, fp, httpServletRequest)
        return getModelAndView()
            .addObject("linkList", linkList)
            .addObject("linkListText", getLinkListText(linkList))
            .addObject("linkSpray", shortenedSprayUrl)
            .addObject("linkSprayShortened", true)
    }

    @GetMapping("/open")
    fun getSpray(
        @RequestParam(name = "spray") sprayStringList: List<String>
    ): ModelAndView =
        getModelAndView()
            .addObject("linkListText", getLinkListText(sprayStringList))
            .addObject("spray", sprayStringList)

    override fun getModelAndView(): ModelAndView = getModelAndView(SPRAY)
}
