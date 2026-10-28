package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.MAILTO
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/mailto")
class ViewMailtoController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(MAILTO)

    @GetMapping
    fun home(): ModelAndView = getModelAndView()
}
