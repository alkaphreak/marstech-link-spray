package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.ImageEntity
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

interface ImageService {
    /**
     * Uploads [file] on behalf of the app identified by [rawApiKey]. Throws
     * [IllegalAccessException] if the API key is invalid, [IllegalArgumentException] if the file
     * is missing/empty or exceeds the maximum allowed size.
     */
    fun uploadImage(
        rawApiKey: String?,
        file: MultipartFile,
        isPrivate: Boolean,
        httpServletRequest: HttpServletRequest,
    ): ImageEntity

    /**
     * Retrieves the metadata for [id], enforcing visibility rules: public images are always
     * readable, private images require a valid [rawApiKey] matching the owning app.
     * Throws [NoSuchElementException] if not found, [IllegalAccessException] if access is denied.
     */
    fun getImage(id: String, rawApiKey: String?): ImageEntity

    /**
     * Streams the binary content of image [id], enforcing the same visibility rules as
     * [getImage].
     */
    fun downloadImage(id: String, rawApiKey: String?): Pair<ImageEntity, InputStream>

    /**
     * Deletes image [id]. Only the owning app (via [rawApiKey]) may delete its own images.
     */
    fun deleteImage(id: String, rawApiKey: String?)

    /**
     * Lists all images belonging to the app identified by [rawApiKey].
     */
    fun listImages(rawApiKey: String?): List<ImageEntity>
}
