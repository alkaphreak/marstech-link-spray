package fr.marstech.mtlinkspray.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

/**
 * Metadata for an image uploaded through the image storage service (MLS-203).
 * The binary content itself lives on the S3-compatible backend (Digital Ocean Spaces),
 * referenced here by [s3Key]. Retention is permanent by default ([expiresAt] null),
 * per product decision.
 */
@Document(collection = "images")
data class ImageEntity(
    @Id override val id: String = UUID.randomUUID().toString(),
    override val creationDate: LocalDateTime = LocalDateTime.now(),
    override val expiresAt: LocalDateTime? = null,
    override var isEnabled: Boolean = true,
    override var description: String? = null,
    override var metadata: MutableMap<String, String> = mutableMapOf(),
    override var author: HistoryItem,
    override var historyItems: MutableList<HistoryItem> = mutableListOf(),

    @Indexed val apiKeyId: String,
    @Indexed(unique = true) val s3Key: String,
    val originalFilename: String,
    val contentType: String,
    val sizeBytes: Long,
    val isPrivate: Boolean = false,
) : StandardEntity
