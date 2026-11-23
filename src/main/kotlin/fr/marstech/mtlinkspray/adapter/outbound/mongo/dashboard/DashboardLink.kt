package fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard

import org.springframework.data.annotation.TypeAlias
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ValidUrl

@TypeAlias("fr.marstech.mtlinkspray.entity.DashboardLink")
data class DashboardLink(
    override var id: String = java.util.UUID.randomUUID().toString(),
    override var name: String,
    override var description: String? = null,
    // http(s) only: the public page renders it as an href (MLS-115)
    @field:ValidUrl var url: String? = null
) : DashboardItem
