package fr.marstech.mtlinkspray.domain.dashboard
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class DashboardServiceImpl(
    private val dashboardStore: DashboardStore,
    private val apiKeyResolver: ApiKeyResolver,
) : DashboardService {

    override fun createDashboard(dashboard: Dashboard, rawApiKey: String?): Dashboard {
        val apiKeyId = rawApiKey?.let { apiKeyResolver.resolve(it).apiKeyId }
        checkSlugFree(dashboard.slug, dashboard.id)
        return dashboardStore.create(dashboard.copy(apiKeyId = apiKeyId, isEnabled = true))
    }

    override fun createDashboard(dashboardName: String): Dashboard =
        createDashboard(Dashboard(name = dashboardName))

    override fun getDashboard(id: String): Dashboard = findById(id)

    override fun getDashboardBySlug(slug: String): Dashboard =
        dashboardStore.findBySlug(slug)?.takeIf { it.isEnabled }
            ?: throw NoSuchElementException("Page not found")

    override fun updateDashboard(id: String, dashboard: Dashboard, rawApiKey: String?): Dashboard {
        val stored = findById(id)
        // Credentials first: a missing or invalid key is 401 everywhere, 403 is only for a valid key
        val caller = apiKeyResolver.resolve(rawApiKey)
        if (stored.apiKeyId == null) throw ResponseStatusException(FORBIDDEN, "Dashboard created without an API key is read-only")
        if (caller.apiKeyId != stored.apiKeyId) throw ResponseStatusException(FORBIDDEN, "Not authorized to edit this dashboard")
        checkSlugFree(dashboard.slug, id)
        // The store replaces the content fields only: owner, enabled flag and creation data stay as stored
        return dashboardStore.update(dashboard.copy(id = id))
    }

    private fun findById(id: String): Dashboard =
        dashboardStore.findById(id) ?: throw NoSuchElementException("Dashboard not found")

    // Check-then-save gives a readable error; the store's unique slug index makes the loser of a concurrent claim fail
    private fun checkSlugFree(slug: String?, ownId: String) {
        if (slug != null && dashboardStore.findBySlug(slug)?.takeIf { it.id != ownId } != null)
            throw DuplicateKeyException("Slug '$slug' is already taken")
    }
}
