package fr.marstech.mtlinkspray.adapter.inbound.web.paste
import fr.marstech.mtlinkspray.domain.paste.Paste
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
        fun from(paste: Paste): PasteResponse {
            return PasteResponse(
                id = paste.id,
                title = paste.title,
                content = paste.content,
                language = paste.language.name,
                createdAt = paste.creationDate,
                expiresAt = paste.expiresAt,
                isPrivate = paste.isPrivate,
                isPasswordProtected = paste.isPasswordProtected,
            )
        }
    }
}