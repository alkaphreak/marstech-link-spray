package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.entity.ImageEntity
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
        fun fromEntity(entity: ImageEntity): ImageUploadResponse = ImageUploadResponse(
            id = entity.id,
            originalFilename = entity.originalFilename,
            contentType = entity.contentType,
            sizeBytes = entity.sizeBytes,
            isPrivate = entity.isPrivate,
            creationDate = entity.creationDate,
        )
    }
}
