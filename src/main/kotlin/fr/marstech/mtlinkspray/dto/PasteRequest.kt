package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.enums.ExpirationEnum
import fr.marstech.mtlinkspray.enums.PastebinTextLanguageEnum
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PasteRequest(
    @field:Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    val title: String? = null,

    @field:NotBlank(message = "Content cannot be blank")
    @field:Size(min = 1, max = 500000, message = "Content must be between 1 and 500000 characters")
    val content: String,

    @field:NotBlank(message = "Language cannot be blank")
    val language: String = PastebinTextLanguageEnum.TEXT.name,

    @field:Size(min = 4, max = 100, message = "Password must be between 4 and 100 characters")
    val password: String? = null,

    @field:NotBlank(message = "Expiration cannot be blank")
    val expiration: String = ExpirationEnum.NEVER.expiration,

    val isPrivate: Boolean = false,
)