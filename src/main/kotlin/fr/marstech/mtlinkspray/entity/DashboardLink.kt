package fr.marstech.mtlinkspray.entity

data class DashboardLink(
    override var id: String = java.util.UUID.randomUUID().toString(),
    override var name: String,
    override var description: String? = null,

    private var url: String? = null
) : DashboardItem
