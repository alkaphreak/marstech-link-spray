package fr.marstech.mtlinkspray.controller.api;

import fr.marstech.mtlinkspray.dto.DashboardDto;
import fr.marstech.mtlinkspray.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard/{id}")
    public DashboardDto getDashboard(@PathVariable(name = "id") String id) {
        return dashboardService.getDashboard(id);
    }

    @PostMapping("/api/dashboard")
    public DashboardDto createDashboard(DashboardDto dashboardDto) {
        return dashboardService.createDashboard(dashboardDto);
    }

    @PutMapping("/api/dashboard/{id}")
    public DashboardDto updateDashboard(@PathVariable(name = "id") String id, @RequestBody DashboardDto dashboardDto) {
        return dashboardService.updateDashboard(id, dashboardDto);
    }
}
