package fr.marstech.mtlinkspray.adapter.inbound.web.image
import fr.marstech.mtlinkspray.domain.image.Image
import java.time.LocalDateTime

/**
 * Image metadata returned when listing images for an app (MLS-203 image storage service).
 */
data class ImageMetadataDto(
    val id: String,
    val originalFilename: String,
    val contentType: String,
    val sizeBytes: Long,
    val isPrivate: Boolean,
    val creationDate: LocalDateTime,
) {
    companion object {
        fun from(image: Image): ImageMetadataDto = ImageMetadataDto(
            id = image.id,
            originalFilename = image.originalFilename,
            contentType = image.contentType,
            sizeBytes = image.sizeBytes,
            isPrivate = image.isPrivate,
            creationDate = image.creationDate,
        )
    }
}
