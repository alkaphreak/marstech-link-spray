package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.SPRAY
import fr.marstech.mtlinkspray.service.SprayService.Companion.getLinkList
import fr.marstech.mtlinkspray.service.SprayService.Companion.getLinkListText
import fr.marstech.mtlinkspray.service.SprayService.Companion.getLinkSpray
import fr.marstech.mtlinkspray.utils.NetworkUtils.getHeadersAsMap
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/spray")
class ViewSprayController : ThymeleafViewControllerInterface {

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

    @GetMapping("/open")
    fun getSpray(
        @RequestParam(name = "spray") sprayStringList: List<String>
    ): ModelAndView =
        getModelAndView()
            .addObject("linkListText", getLinkListText(sprayStringList))
            .addObject("spray", sprayStringList)

    override fun getModelAndView(): ModelAndView = getModelAndView(SPRAY)
}
