package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.entity.DashboardItem
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder
import java.util.UUID

data class DashboardDto(
    val id: String = UUID.randomUUID().toString(),
    val name: String? = null,
    val items: MutableList<DashboardItem> = mutableListOf(),
    val description: String? = null,
)