package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.HOME
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class ViewRootController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(HOME)

    @GetMapping("index")
    fun getIndex(): String = getRedirectUrl("/")

    @GetMapping
    fun getHome(): ModelAndView = getModelAndView()
}
