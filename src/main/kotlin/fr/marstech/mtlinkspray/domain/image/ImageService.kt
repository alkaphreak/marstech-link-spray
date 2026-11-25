package fr.marstech.mtlinkspray.domain.image
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import java.io.InputStream

interface ImageService {
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
    ): Image

    /**
     * Retrieves the metadata for [id], enforcing visibility rules: public images are always
     * readable, private images require a valid [rawApiKey] matching the owning app.
     * Throws [NoSuchElementException] if not found, [IllegalAccessException] for a missing or invalid key, [fr.marstech.mtlinkspray.domain.shared.ForbiddenException] (403) for another app's private image.
     */
    fun getImage(id: String, rawApiKey: String?): Image

    /**
     * Streams the binary content of image [id], enforcing the same visibility rules as
     * [getImage].
     */
    fun downloadImage(id: String, rawApiKey: String?): Pair<Image, InputStream>

    /**
     * Deletes image [id]. Only the owning app (via [rawApiKey]) may delete its own images.
     */
    fun deleteImage(id: String, rawApiKey: String?)

    /**
     * Lists all images belonging to the app identified by [rawApiKey].
     */
    fun listImages(rawApiKey: String?): List<Image>
}
