package fr.marstech.mtlinkspray.domain.dashboard

interface DashboardService {
    /** [rawApiKey] (optional) becomes the owner; without it the dashboard is read-only. [Dashboard.apiKeyId] is ignored. */
    fun createDashboard(dashboard: Dashboard, rawApiKey: String? = null): Dashboard

    fun createDashboard(dashboardName: String): Dashboard

    fun getDashboard(id: String): Dashboard

    /** Public page lookup; throws [NoSuchElementException] if unknown or disabled. */
    fun getDashboardBySlug(slug: String): Dashboard

    /** Throws [IllegalAccessException] for a missing or invalid key, 403 [org.springframework.web.server.ResponseStatusException] unless [rawApiKey] is the key that created the dashboard. */
    fun updateDashboard(id: String, dashboard: Dashboard, rawApiKey: String?): Dashboard
}
