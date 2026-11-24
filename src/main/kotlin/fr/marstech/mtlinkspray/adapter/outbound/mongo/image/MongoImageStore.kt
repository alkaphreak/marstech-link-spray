package fr.marstech.mtlinkspray.adapter.outbound.mongo.image

import fr.marstech.mtlinkspray.domain.image.Image
import fr.marstech.mtlinkspray.domain.image.ImageStore
import org.springframework.stereotype.Component

@Component
class MongoImageStore(
    private val repository: ImageRepository,
) : ImageStore {

    // insert, not save: a full write of an existing id would drop the fields the domain does not carry
    override fun create(image: Image): Image = repository.insert(ImageEntity.of(image)).toDomain()

    override fun findById(id: String): Image? = repository.findById(id).orElse(null)?.toDomain()

    override fun findByApiKeyId(apiKeyId: String): List<Image> = repository.findByApiKeyId(apiKeyId).map { it.toDomain() }

    override fun deleteById(id: String) = repository.deleteById(id)
}
