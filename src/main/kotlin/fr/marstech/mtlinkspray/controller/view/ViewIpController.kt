package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.IP
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/ip")
class ViewIpController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(IP)

    @GetMapping
    fun home(): ModelAndView = getModelAndView()
}
