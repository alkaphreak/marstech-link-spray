package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.api.ApiUuidController
import fr.marstech.mtlinkspray.enums.ViewNameEnum
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class ViewUuidController(
    private val apiUuidController: ApiUuidController
) : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(ViewNameEnum.UUID)

    @GetMapping("/uuid")
    fun home(): ModelAndView = getModelAndView().addObject("uuid", apiUuidController.uuid)
}