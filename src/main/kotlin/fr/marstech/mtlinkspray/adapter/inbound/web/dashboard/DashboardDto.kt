package fr.marstech.mtlinkspray.adapter.inbound.web.dashboard
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ValidUrl
import fr.marstech.mtlinkspray.domain.dashboard.Dashboard
import fr.marstech.mtlinkspray.domain.dashboard.DashboardLink
import fr.marstech.mtlinkspray.domain.dashboard.DashboardTheme
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.*

data class DashboardDto(
    val id: String = UUID.randomUUID().toString(),
    val name: String? = null,
    @field:Valid val items: MutableList<DashboardLinkDto> = mutableListOf(),
    val description: String? = null,
    /** Public address `/p/{slug}`, unique. */
    @field:Pattern(regexp = SLUG_PATTERN, message = "slug must be 3-32 chars of a-z, 0-9 and '-'")
    val slug: String? = null,
    @field:Size(max = 280) val bio: String? = null,
    @field:ValidUrl(protocols = ["https"]) val avatarUrl: String? = null,
    val theme: DashboardTheme = DashboardTheme.DARK,
) {
    fun toDashboard() = Dashboard(id, name, items.map { it.toDomain() }, description, slug, bio, avatarUrl, theme)

    companion object {
        const val SLUG_PATTERN = "^[a-z0-9-]{3,32}$"

        fun from(dashboard: Dashboard) = DashboardDto(
            dashboard.id, dashboard.name,
            dashboard.items.mapTo(mutableListOf()) { item ->
                when (item) {
                    is DashboardLink -> DashboardLinkDto(item.id, item.name, item.description, item.url)
                }
            },
            dashboard.description, dashboard.slug, dashboard.bio, dashboard.avatarUrl, dashboard.theme,
        )
    }
}

/** The only dashboard item type. */
data class DashboardLinkDto(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    // http(s) only: the public page renders it as an href (MLS-115)
    @field:ValidUrl val url: String? = null,
) {
    fun toDomain() = DashboardLink(id, name, description, url)
}
