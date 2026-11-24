package fr.marstech.mtlinkspray.domain.image

import java.io.InputStream
import java.time.LocalDateTime
import java.util.UUID

/** Metadata of a stored image (MLS-203); the bytes live in the object storage under [storageKey]. */
data class Image(
    val id: String = UUID.randomUUID().toString(),
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val apiKeyId: String,
    val storageKey: String,
    val originalFilename: String,
    val contentType: String,
    val sizeBytes: Long,
    val isPrivate: Boolean = false,
    val authorIpAddress: String? = null,
)

/**
 * An uploaded file as the web adapter received it: the client's file name, the part size measured by the
 * server, and its content, opened only once the upload passed the key and size checks.
 */
class ImageUpload(val originalFilename: String?, val sizeBytes: Long, val content: () -> InputStream)
