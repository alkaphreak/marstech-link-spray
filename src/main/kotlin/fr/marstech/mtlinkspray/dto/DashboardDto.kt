package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.entity.DashboardItem
import java.util.*

data class DashboardDto(
    val id: String = UUID.randomUUID().toString(),
    val name: String? = null,
    val items: MutableList<DashboardItem> = mutableListOf(),
    val description: String? = null,
)