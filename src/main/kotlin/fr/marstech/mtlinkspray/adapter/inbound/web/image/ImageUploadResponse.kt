package fr.marstech.mtlinkspray.adapter.inbound.web.image
import fr.marstech.mtlinkspray.domain.image.Image
import java.time.LocalDateTime

/**
 * Response returned right after an image upload (MLS-203 image storage service).
 */
data class ImageUploadResponse(
    val id: String,
    val originalFilename: String,
    val contentType: String,
    val sizeBytes: Long,
    val isPrivate: Boolean,
    val creationDate: LocalDateTime,
) {
    companion object {
        fun from(image: Image): ImageUploadResponse = ImageUploadResponse(
            id = image.id,
            originalFilename = image.originalFilename,
            contentType = image.contentType,
            sizeBytes = image.sizeBytes,
            isPrivate = image.isPrivate,
            creationDate = image.creationDate,
        )
    }
}
