package fr.marstech.mtlinkspray.domain.paste

interface PasteStore {
    /** Stores a new paste; the domain never rewrites an existing one. */
    fun create(paste: Paste): Paste

    fun findById(id: String): Paste?

    fun deleteById(id: String)
}
