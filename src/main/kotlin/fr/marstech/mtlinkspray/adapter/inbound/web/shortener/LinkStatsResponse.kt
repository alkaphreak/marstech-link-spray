package fr.marstech.mtlinkspray.adapter.inbound.web.shortener
import java.time.LocalDateTime

data class LinkStatsResponse(
    val clickCount: Long,
    val lastClickedAt: LocalDateTime?,
    val botClickCount: Long,
)
