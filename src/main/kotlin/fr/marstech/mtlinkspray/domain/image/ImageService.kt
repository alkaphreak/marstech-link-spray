package fr.marstech.mtlinkspray.domain.image
import fr.marstech.mtlinkspray.domain.shared.ForbiddenException
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.shared.ImageType
import java.io.InputStream

class ImageService(
    private val imageStore: ImageStore,
    private val apiKeyResolver: ApiKeyResolver,
    private val imageStorage: ImageStoragePort,
    private val maxFileSizeBytes: Long,
) {

    /**
     * Uploads [upload] on behalf of the app identified by [rawApiKey]. Throws
     * [IllegalAccessException] if the API key is invalid, [IllegalArgumentException] if the file
     * is missing/empty or exceeds the maximum allowed size.
     */
    fun uploadImage(
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
        val contentType = ImageType.detect(bytes)?.mediaType ?: throw IllegalArgumentException(
            "Unsupported file type: only ${ImageType.MEDIA_TYPES.joinToString()} are accepted"
        )
        val storageKey = imageStorage.upload(apiKey.namespace, filename, contentType, bytes)

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

    /**
     * Retrieves the metadata for [id], enforcing visibility rules: public images are always
     * readable, private images require a valid [rawApiKey] matching the owning app.
     * Throws [NoSuchElementException] if not found, [IllegalAccessException] for a missing or invalid key, [fr.marstech.mtlinkspray.domain.shared.ForbiddenException] (403) for another app's private image.
     */
    fun getImage(id: String, rawApiKey: String?): Image =
        findImage(id).also { checkAccess(it, rawApiKey) }

    /**
     * Streams the binary content of image [id], enforcing the same visibility rules as
     * [getImage].
     */
    fun downloadImage(id: String, rawApiKey: String?): Pair<Image, InputStream> {
        val image = getImage(id, rawApiKey)
        return image to imageStorage.download(image.storageKey)
    }

    /**
     * Deletes image [id]. Only the owning app (via [rawApiKey]) may delete its own images.
     */
    fun deleteImage(id: String, rawApiKey: String?) {
        val apiKey = apiKeyResolver.resolve(rawApiKey)
        val image = findImage(id)
        if (image.apiKeyId != apiKey.apiKeyId) throw ForbiddenException("Not authorized to delete this image")

        imageStorage.delete(image.storageKey)
        imageStore.deleteById(id)
    }

    /**
     * Lists all images belonging to the app identified by [rawApiKey].
     */
    fun listImages(rawApiKey: String?): List<Image> {
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
