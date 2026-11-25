package fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard

/** Stored item of [DashboardEntity.items]; [DashboardLinkEntity] is the only type. */
interface DashboardItemEntity {
    var id: String
    var name: String
    var description: String?
}
