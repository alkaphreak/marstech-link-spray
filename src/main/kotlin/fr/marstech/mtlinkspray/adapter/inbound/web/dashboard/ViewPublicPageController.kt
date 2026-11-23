package fr.marstech.mtlinkspray.adapter.inbound.web.dashboard
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ThymeleafViewControllerInterface
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.DASHBOARD
import fr.marstech.mtlinkspray.domain.dashboard.DashboardService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView

/** Linktree-style public page of a dashboard, by its vanity slug (MLS-115). Read-only. */
@Controller
class ViewPublicPageController(val dashboardService: DashboardService) : ThymeleafViewControllerInterface {

    @GetMapping("/p/{slug}")
    fun getPage(@PathVariable slug: String, httpServletRequest: HttpServletRequest): ModelAndView =
        getModelAndView(DASHBOARD, httpServletRequest).addObject("dashboard", dashboardService.getDashboardBySlug(slug))

    override fun getModelAndView(): ModelAndView = getModelAndView(DASHBOARD)
}
