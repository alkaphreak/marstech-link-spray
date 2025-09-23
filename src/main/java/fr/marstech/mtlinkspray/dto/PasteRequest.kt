package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.enums.ExpirationEnum
import fr.marstech.mtlinkspray.enums.PastebinTextLanguageEnum

data class PasteRequest(
    val title: String? = null,
    val content: String,
    val language: String = PastebinTextLanguageEnum.TEXT.name,
    val password: String? = null,
    val expiration: String = ExpirationEnum.NEVER.expiration,
    val isPrivate: Boolean = false,
)