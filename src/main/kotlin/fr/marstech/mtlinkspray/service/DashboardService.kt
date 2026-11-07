package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.DashboardDto

interface DashboardService {
    /** [rawApiKey] (optional) becomes the owner; without it the dashboard is read-only. */
    fun createDashboard(dashboardDto: DashboardDto, rawApiKey: String? = null): DashboardDto

    fun createDashboard(dashboardName: String): DashboardDto

    fun getDashboard(id: String): DashboardDto

    /** Public page lookup; throws [NoSuchElementException] if unknown or disabled. */
    fun getDashboardBySlug(slug: String): DashboardDto

    /** Throws [IllegalAccessException] unless [rawApiKey] is the key that created the dashboard. */
    fun updateDashboard(id: String, dashboardDto: DashboardDto, rawApiKey: String?): DashboardDto
}
