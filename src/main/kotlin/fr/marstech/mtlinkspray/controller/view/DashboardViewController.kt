package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.DASHBOARD
import fr.marstech.mtlinkspray.service.DashboardService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/dashboard")
class DashboardViewController(val dashboardService: DashboardService) : ThymeleafViewControllerInterface {

    @GetMapping
    fun getView(httpServletRequest: HttpServletRequest): ModelAndView = getModelAndView(httpServletRequest)

    @PostMapping
    fun postView(@RequestParam(name = "input-dashboard-name") @NotBlank inputDashboardName: String): String =
        getRedirectUrl("/dashboard/${dashboardService.createDashboard(inputDashboardName).id}")

    @GetMapping("/{id}")
    fun getDashboardById(
        @PathVariable(name = "id") @NotBlank id: String, httpServletRequest: HttpServletRequest
    ): ModelAndView = getModelAndView(httpServletRequest).addObject("dashboard", dashboardService.getDashboard(id))

    override fun getModelAndView(): ModelAndView = getModelAndView(DASHBOARD)

    fun getModelAndView(httpServletRequest: HttpServletRequest): ModelAndView =
        getModelAndView(DASHBOARD, httpServletRequest)
}
