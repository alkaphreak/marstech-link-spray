package fr.marstech.mtlinkspray.entity

import fr.marstech.mtlinkspray.enums.DashboardTheme
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document(collection = "mt-link-spray-dashboard-entity")
data class DashboardEntity(
    @Id override val id: String = UUID.randomUUID().toString(),
    override val creationDate: LocalDateTime = LocalDateTime.now(),
    override val expiresAt: LocalDateTime? = null,
    override var isEnabled: Boolean = true,
    override var description: String? = null,
    override var metadata: MutableMap<String, String> = mutableMapOf(),
    override var author: HistoryItem,
    override var historyItems: MutableList<HistoryItem> = mutableListOf(),
    val name: String? = null,
    val items: MutableList<DashboardItem> = mutableListOf(),
    @Indexed(unique = true, sparse = true) val slug: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val theme: DashboardTheme = DashboardTheme.DARK,
    // Key that created the dashboard; only it can edit. Null = created anonymously, read-only (MLS-115).
    @Indexed val apiKeyId: String? = null,
) : StandardEntity
