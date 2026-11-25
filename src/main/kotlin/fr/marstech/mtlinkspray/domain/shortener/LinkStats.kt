package fr.marstech.mtlinkspray.domain.shortener
import java.time.LocalDateTime

/** Click counts (people, robots) and last click date of a short link (MLS-261, MLS-273). */
data class LinkStats(
    val clickCount: Long,
    val lastClickedAt: LocalDateTime?,
    val botClickCount: Long,
)
