package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.HOME
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class ViewRootController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(HOME)

    @GetMapping("/index")
    fun getIndex(): ModelAndView = getModelAndView()

    @GetMapping("/")
    fun getHome(): ModelAndView = getModelAndView()
}
