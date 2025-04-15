package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import fr.marstech.mtlinkspray.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static fr.marstech.mtlinkspray.enums.ViewNameEnum.DASHBOARD;
import static fr.marstech.mtlinkspray.utils.NetworkUtils.getHeadersAsMap;

@Controller
@Log
public class DashboardController implements ThymeleafViewControllerInterface {

    private static final ViewNameEnum viewNameEnum = DASHBOARD;

    final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ModelAndView getView(HttpServletRequest httpServletRequest) {
        return getModelAndView().addObject("headers", getHeadersAsMap(httpServletRequest));
    }

    @PostMapping("/dashboard")
    public ModelAndView postView(
            @RequestParam(name = "input-dashboard-name") String inputDashboardName,
            HttpServletRequest httpServletRequest
    ) {
        return getModelAndView()
                .addObject("headers", getHeadersAsMap(httpServletRequest))
                .addObject("dashboard", dashboardService.createDashboard(inputDashboardName));
    }

    @Override
    public ModelAndView getModelAndView() {
        return getModelAndView(viewNameEnum);
    }
}
