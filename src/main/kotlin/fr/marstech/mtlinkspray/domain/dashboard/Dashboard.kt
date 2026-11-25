package fr.marstech.mtlinkspray.domain.dashboard
import java.util.UUID

/** A link page (MLS-115): public at `/p/{slug}` when it has one and is enabled. */
data class Dashboard(
    val id: String = UUID.randomUUID().toString(),
    val name: String? = null,
    val items: List<DashboardItem> = emptyList(),
    val description: String? = null,
    val slug: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val theme: DashboardTheme = DashboardTheme.DARK,
    /** Key that created the dashboard; only it can edit. Null = created anonymously, read-only. */
    val apiKeyId: String? = null,
    val isEnabled: Boolean = true,
)

sealed interface DashboardItem {
    val id: String
    val name: String
    val description: String?
}

/** [url] is validated (http(s) only) by the web adapter before it reaches the domain. */
data class DashboardLink(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String,
    override val description: String? = null,
    val url: String? = null,
) : DashboardItem
