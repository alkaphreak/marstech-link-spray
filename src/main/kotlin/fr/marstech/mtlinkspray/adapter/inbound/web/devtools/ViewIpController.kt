package fr.marstech.mtlinkspray.adapter.inbound.web.devtools
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ThymeleafViewControllerInterface
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.IP
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/ip")
class ViewIpController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(IP)

    @GetMapping
    fun home(): ModelAndView = getModelAndView()
}
