package fr.marstech.mtlinkspray.adapter.outbound.mongo.paste

import fr.marstech.mtlinkspray.domain.paste.Paste
import fr.marstech.mtlinkspray.domain.paste.PasteStore
import org.springframework.stereotype.Component

@Component
class MongoPasteStore(
    private val repository: PasteRepository,
) : PasteStore {

    // insert, not save: a full write of an existing id would drop the fields the domain does not carry
    override fun create(paste: Paste): Paste = repository.insert(PasteEntity.of(paste)).toDomain()

    override fun findById(id: String): Paste? = repository.findById(id).orElse(null)?.toDomain()

    override fun deleteById(id: String) = repository.deleteById(id)
}
