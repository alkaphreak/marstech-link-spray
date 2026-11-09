package fr.marstech.mtlinkspray.dto

import java.time.LocalDateTime

data class LinkStatsResponse(
    val clickCount: Long,
    val lastClickedAt: LocalDateTime?,
)
