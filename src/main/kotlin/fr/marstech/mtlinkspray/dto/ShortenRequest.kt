package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.validation.ValidUrl
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ShortenRequest(
    @field:NotBlank(message = "URL cannot be blank")
    @field:ValidUrl(message = "Invalid URL format or protocol")
    @field:Size(max = 2048, message = "URL cannot exceed 2048 characters")
    val url: String,
)
