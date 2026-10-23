package fr.marstech.mtlinkspray.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class QrCodeRequest(
    @field:NotBlank(message = "Text cannot be blank")
    @field:Size(max = 2000, message = "Text must not exceed 2000 characters")
    val text: String? = null,

    @field:Min(value = 64, message = "Size must be at least 64 pixels")
    @field:Max(value = 1024, message = "Size must not exceed 1024 pixels")
    val size: Int = 256,

    @field:Pattern(regexp = "^[0-9a-fA-F]{6}$", message = "Foreground color must be a RRGGBB hex value")
    val fg: String = "000000",

    @field:Pattern(regexp = "^[0-9a-fA-F]{6}$", message = "Background color must be a RRGGBB hex value")
    val bg: String = "ffffff",

    @field:Pattern(regexp = "(?i)^(png|svg)$", message = "Format must be png or svg")
    val format: String = "png",

    val download: Boolean = false,
) {
    val isSvg: Boolean get() = format.equals("svg", ignoreCase = true)
}
