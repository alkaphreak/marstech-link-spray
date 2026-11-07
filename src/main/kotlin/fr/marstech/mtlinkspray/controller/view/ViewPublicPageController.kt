package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.DASHBOARD
import fr.marstech.mtlinkspray.service.DashboardService
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
