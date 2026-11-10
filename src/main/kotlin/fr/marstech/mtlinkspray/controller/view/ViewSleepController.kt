package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.SLEEP
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/sleep")
class ViewSleepController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(SLEEP)

    @GetMapping
    fun viewSleep(): ModelAndView = getModelAndView()
}
