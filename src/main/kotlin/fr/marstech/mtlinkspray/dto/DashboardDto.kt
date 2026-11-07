package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.entity.DashboardItem
import fr.marstech.mtlinkspray.enums.DashboardTheme
import fr.marstech.mtlinkspray.validation.ValidUrl
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.*

data class DashboardDto(
    val id: String = UUID.randomUUID().toString(),
    val name: String? = null,
    @field:Valid val items: MutableList<DashboardItem> = mutableListOf(),
    val description: String? = null,
    /** Public address `/p/{slug}`, unique. */
    @field:Pattern(regexp = SLUG_PATTERN, message = "slug must be 3-32 chars of a-z, 0-9 and '-'")
    val slug: String? = null,
    @field:Size(max = 280) val bio: String? = null,
    @field:ValidUrl(protocols = ["https"]) val avatarUrl: String? = null,
    val theme: DashboardTheme = DashboardTheme.DARK,
) {
    companion object {
        const val SLUG_PATTERN = "^[a-z0-9-]{3,32}$"
    }
}
