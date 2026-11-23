package fr.marstech.mtlinkspray.domain.dashboard
import fr.marstech.mtlinkspray.adapter.inbound.web.dashboard.DashboardDto

interface DashboardService {
    /** [rawApiKey] (optional) becomes the owner; without it the dashboard is read-only. */
    fun createDashboard(dashboardDto: DashboardDto, rawApiKey: String? = null): DashboardDto

    fun createDashboard(dashboardName: String): DashboardDto

    fun getDashboard(id: String): DashboardDto

    /** Public page lookup; throws [NoSuchElementException] if unknown or disabled. */
    fun getDashboardBySlug(slug: String): DashboardDto

    /** Throws [IllegalAccessException] for a missing or invalid key, 403 [org.springframework.web.server.ResponseStatusException] unless [rawApiKey] is the key that created the dashboard. */
    fun updateDashboard(id: String, dashboardDto: DashboardDto, rawApiKey: String?): DashboardDto
}
