package fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard
import tools.jackson.databind.annotation.JsonDeserialize

// DashboardLink is the only item type: without this, Jackson cannot build the interface from JSON.
@JsonDeserialize(`as` = DashboardLink::class)
interface DashboardItem {
    var id: String
    var name: String
    var description: String?
}
