package fr.marstech.mtlinkspray.entity

import java.util.*

data class DashboardCategory(
    override var id: String = UUID.randomUUID().toString(),
    override var name: String,
    override var description: String?,

    val links: MutableList<DashboardLink?> = mutableListOf<DashboardLink?>()
) : DashboardItem
