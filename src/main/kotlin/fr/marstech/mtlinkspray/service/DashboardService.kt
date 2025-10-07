package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.DashboardDto

interface DashboardService {
    fun createDashboard(dashboardDto: DashboardDto): DashboardDto

    fun createDashboard(dashboardName: String): DashboardDto

    fun getDashboard(id: String): DashboardDto

    fun updateDashboard(id: String, dashboardDto: DashboardDto): DashboardDto
}
