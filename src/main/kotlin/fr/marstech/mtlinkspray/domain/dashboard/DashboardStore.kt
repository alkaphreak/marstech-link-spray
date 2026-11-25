package fr.marstech.mtlinkspray.domain.dashboard

/** Dashboard storage, implemented by the Mongo adapter, which also owns the unique slug index. */
interface DashboardStore {

    /** The id comes from the client: an existing id fails instead of being overwritten. */
    fun create(dashboard: Dashboard): Dashboard

    /** Replaces the content fields (name, items, description, slug, bio, avatar, theme) of a stored dashboard. */
    fun update(dashboard: Dashboard): Dashboard

    fun findById(id: String): Dashboard?

    fun findBySlug(slug: String): Dashboard?
}
