package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.DNS
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/dns")
class ViewDnsController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(DNS)

    @GetMapping
    fun home(): ModelAndView = getModelAndView()
}
