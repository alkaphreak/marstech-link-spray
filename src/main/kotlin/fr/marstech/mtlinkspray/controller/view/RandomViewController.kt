package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.RANDOM
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/random")
class RandomViewController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(RANDOM)

    @GetMapping
    fun viewRandom(): ModelAndView = getModelAndView()
}