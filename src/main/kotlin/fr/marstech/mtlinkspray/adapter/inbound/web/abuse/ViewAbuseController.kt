package fr.marstech.mtlinkspray.adapter.inbound.web.abuse
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ThymeleafViewControllerInterface
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.ABUSE
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.clientContext
import fr.marstech.mtlinkspray.domain.abuse.ReportAbuseService
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.getHeadersAsMap
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/abuse")
class ViewAbuseController(private val reportAbuseService: ReportAbuseService) : ThymeleafViewControllerInterface {

    @GetMapping
    fun getView(httpServletRequest: HttpServletRequest): ModelAndView =
        getModelAndView().addObject("headers", getHeadersAsMap(httpServletRequest))

    @PostMapping
    fun postAbuseForm(
        @RequestParam @NotBlank inputAbuseDecsription: String,
        httpServletRequest: HttpServletRequest
    ): ModelAndView = reportAbuseService
        .reportAbuse(inputAbuseDecsription, clientContext(httpServletRequest))
        .let { getModelAndView() }
        .addObject("headers", getHeadersAsMap(httpServletRequest))
        .addObject("abuseReported", true)

    override fun getModelAndView(): ModelAndView = getModelAndView(ABUSE)
}
