package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.entity.PasteEntity
import java.time.LocalDateTime

data class PasteResponse(
    val id: String,
    val title: String?,
    val content: String,
    val language: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val isPrivate: Boolean,
    val isPasswordProtected: Boolean,
) {
    companion object {
        fun fromEntity(pasteEntity: PasteEntity): PasteResponse {
            return PasteResponse(
                id = pasteEntity.id,
                title = pasteEntity.title,
                content = pasteEntity.content,
                language = pasteEntity.language,
                createdAt = pasteEntity.creationDate,
                expiresAt = pasteEntity.expiresAt,
                isPrivate = pasteEntity.isPrivate,
                isPasswordProtected = pasteEntity.isPasswordProtected,
            )
        }
    }
}