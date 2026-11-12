package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.CRON
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/cron")
class ViewCronController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(CRON)

    @GetMapping
    fun viewCron(): ModelAndView = getModelAndView()
}
