package fr.marstech.mtlinkspray.entity

import java.time.LocalDateTime

data class HistoryItem(
    val ipAddress: String? = null,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val action: String? = null,
)
