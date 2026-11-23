package fr.marstech.mtlinkspray.adapter.inbound.web.devtools
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ThymeleafViewControllerInterface
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.UUID
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/uuid")
class ViewUuidController(
    private val uuidApiController: UuidApiController
) : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(UUID)

    @GetMapping
    fun home(): ModelAndView = getModelAndView().addObject("uuid", uuidApiController.getUuid())
}