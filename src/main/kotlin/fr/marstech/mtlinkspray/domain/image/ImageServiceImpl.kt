package fr.marstech.mtlinkspray.domain.image
import fr.marstech.mtlinkspray.domain.shared.ForbiddenException
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.shared.ImageTypes
import java.io.InputStream

class ImageServiceImpl(
    private val imageStore: ImageStore,
    private val apiKeyResolver: ApiKeyResolver,
    private val imageStorageService: ImageStorageService,
    private val maxFileSizeBytes: Long,
) : ImageService {

    override fun uploadImage(
        rawApiKey: String?,
        upload: ImageUpload,
        isPrivate: Boolean,
        client: ClientContext,
    ): Image {
        val apiKey = apiKeyResolver.resolve(rawApiKey)

        require(upload.sizeBytes != 0L) { "Uploaded file is empty" }
        require(upload.sizeBytes <= maxFileSizeBytes) {
            "File size ${upload.sizeBytes} bytes exceeds the maximum allowed size of $maxFileSizeBytes bytes"
        }

        val filename = upload.originalFilename?.takeIf { it.isNotBlank() } ?: "upload"
        val bytes = upload.content().use { it.readAllBytes() }
        val contentType = ImageTypes.detect(bytes) ?: throw IllegalArgumentException(
            "Unsupported file type: only ${ImageTypes.ALLOWED.joinToString()} are accepted"
        )
        val storageKey = imageStorageService.upload(apiKey.namespace, filename, contentType, bytes)

        return imageStore.create(
            Image(
                apiKeyId = apiKey.apiKeyId,
                storageKey = storageKey,
                originalFilename = filename,
                contentType = contentType,
                sizeBytes = upload.sizeBytes,
                isPrivate = isPrivate,
                authorIpAddress = client.ipAddress,
            )
        )
    }

    override fun getImage(id: String, rawApiKey: String?): Image =
        findImage(id).also { checkAccess(it, rawApiKey) }

    override fun downloadImage(id: String, rawApiKey: String?): Pair<Image, InputStream> {
        val image = getImage(id, rawApiKey)
        return image to imageStorageService.download(image.storageKey)
    }

    override fun deleteImage(id: String, rawApiKey: String?) {
        val apiKey = apiKeyResolver.resolve(rawApiKey)
        val image = findImage(id)
        if (image.apiKeyId != apiKey.apiKeyId) throw ForbiddenException("Not authorized to delete this image")

        imageStorageService.delete(image.storageKey)
        imageStore.deleteById(id)
    }

    override fun listImages(rawApiKey: String?): List<Image> {
        val apiKey = apiKeyResolver.resolve(rawApiKey)
        return imageStore.findByApiKeyId(apiKey.apiKeyId)
    }

    private fun findImage(id: String): Image =
        imageStore.findById(id) ?: throw NoSuchElementException("Image $id not found")

    /**
     * Public images are readable by anyone; private images require a valid API key matching
     * the owning app.
     */
    private fun checkAccess(image: Image, rawApiKey: String?) {
        if (!image.isPrivate) return
        val apiKey = apiKeyResolver.resolve(rawApiKey)
        if (image.apiKeyId != apiKey.apiKeyId) throw ForbiddenException("Not authorized to access this image")
    }
}
