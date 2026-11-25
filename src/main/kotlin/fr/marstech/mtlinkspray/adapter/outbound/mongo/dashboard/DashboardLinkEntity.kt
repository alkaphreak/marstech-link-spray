package fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard

import org.springframework.data.annotation.TypeAlias

/** The alias keeps the pre-move class name stored in `_class` (ADR 0007); URL validation stays on the request DTO. */
@TypeAlias("fr.marstech.mtlinkspray.entity.DashboardLink")
data class DashboardLinkEntity(
    override var id: String = java.util.UUID.randomUUID().toString(),
    override var name: String,
    override var description: String? = null,
    var url: String? = null
) : DashboardItemEntity
