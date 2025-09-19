package fr.marstech.mtlinkspray.utils

import fr.marstech.mtlinkspray.entity.HistoryItem
import jakarta.servlet.http.HttpServletRequest
import java.time.LocalDateTime

object HistoryUtils {
    fun fromRequest(request: HttpServletRequest, action: String): HistoryItem = HistoryItem(
        ipAddress = NetworkUtils.getIpAddress(request),
        dateTime = LocalDateTime.now(),
        action = action
    )
}