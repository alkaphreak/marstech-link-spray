package fr.marstech.mtlinkspray.controller.view;

import fr.marstech.mtlinkspray.dto.DashboardDto;
import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import fr.marstech.mtlinkspray.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static fr.marstech.mtlinkspray.enums.ViewNameEnum.DASHBOARD;
import static java.text.MessageFormat.format;

@Controller
public class DashboardViewController implements ThymeleafViewControllerInterface {

    private static final ViewNameEnum viewNameEnum = DASHBOARD;

    final DashboardService dashboardService;

    public DashboardViewController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ModelAndView getView(HttpServletRequest httpServletRequest) {
        return getModelAndView(httpServletRequest);
    }

    @PostMapping("/dashboard")
    public String postView(@RequestParam(name = "input-dashboard-name") String inputDashboardName) {
        DashboardDto dto = dashboardService.createDashboard(inputDashboardName);
        return getRedirectUrl(format("/dashboard/{0}", dto.getId()));
    }

    @GetMapping("/dashboard/{id}")
    public ModelAndView getDashboardById(
            @PathVariable(name = "id") String id, HttpServletRequest httpServletRequest) {
        return getModelAndView(httpServletRequest).addObject("dashboard", dashboardService.getDashboard(id));
    }

    @Override
    public ModelAndView getModelAndView() {
        return getModelAndView(viewNameEnum);
    }

    public ModelAndView getModelAndView(HttpServletRequest httpServletRequest) {
        return getModelAndView(viewNameEnum, httpServletRequest);
    }
}
