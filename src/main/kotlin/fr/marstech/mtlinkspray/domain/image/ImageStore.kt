package fr.marstech.mtlinkspray.domain.image

interface ImageStore {
    /** Stores new image metadata; the domain never rewrites an existing image. */
    fun create(image: Image): Image

    fun findById(id: String): Image?

    fun findByApiKeyId(apiKeyId: String): List<Image>

    fun deleteById(id: String)
}
