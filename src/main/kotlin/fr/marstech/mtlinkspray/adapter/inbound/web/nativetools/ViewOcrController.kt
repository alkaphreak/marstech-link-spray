package fr.marstech.mtlinkspray.adapter.inbound.web.nativetools
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ThymeleafViewControllerInterface
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.OCR
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/ocr")
class ViewOcrController : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(OCR)

    @GetMapping
    fun home(): ModelAndView = getModelAndView()
}
