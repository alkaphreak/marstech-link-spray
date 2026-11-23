package fr.marstech.mtlinkspray.domain.paste

import java.time.LocalDateTime
import java.util.UUID

data class Paste(
    val id: String = UUID.randomUUID().toString(),
    val title: String? = null,
    val content: String,
    val language: PastebinTextLanguageEnum = PastebinTextLanguageEnum.TEXT,
    val passwordHash: String? = null,
    val isPrivate: Boolean = false,
    val isPasswordProtected: Boolean = false,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime? = null,
    val authorIpAddress: String? = null,
)
