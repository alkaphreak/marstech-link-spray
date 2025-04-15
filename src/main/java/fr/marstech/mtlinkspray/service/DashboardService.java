package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.dto.DashboardDto;

public interface DashboardService {

    DashboardDto createDashboard(DashboardDto dashboardDto);

    DashboardDto createDashboard(String dashboardName);

    DashboardDto getDashboard(String id);
}
