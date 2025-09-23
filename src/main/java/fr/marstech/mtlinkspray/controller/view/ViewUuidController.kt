package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.controller.api.UuidApiController
import fr.marstech.mtlinkspray.enums.ViewNameEnum.UUID
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class ViewUuidController(
    private val uuidApiController: UuidApiController
) : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(UUID)

    @GetMapping("/uuid")
    fun home(): ModelAndView = getModelAndView().addObject("uuid", uuidApiController.uuid)
}