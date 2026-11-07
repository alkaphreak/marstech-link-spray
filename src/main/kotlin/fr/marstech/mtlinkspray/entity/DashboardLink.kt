package fr.marstech.mtlinkspray.entity

import fr.marstech.mtlinkspray.validation.ValidUrl

data class DashboardLink(
    override var id: String = java.util.UUID.randomUUID().toString(),
    override var name: String,
    override var description: String? = null,
    // http(s) only: the public page renders it as an href (MLS-115)
    @field:ValidUrl var url: String? = null
) : DashboardItem
