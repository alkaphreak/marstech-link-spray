package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.PRIVACY
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

/** Privacy notice: what the write signatures store, why and for how long (MLS-254 phase B). */
@Controller
@RequestMapping("/privacy")
class ViewPrivacyController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(PRIVACY)

    @GetMapping
    fun home(): ModelAndView = getModelAndView()
}
