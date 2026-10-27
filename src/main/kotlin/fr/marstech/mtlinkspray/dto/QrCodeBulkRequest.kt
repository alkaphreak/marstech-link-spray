package fr.marstech.mtlinkspray.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/** Style fields are validated per line, as the [QrCodeRequest] built by [forLine]. */
data class QrCodeBulkRequest(
    @field:NotBlank(message = "Text cannot be blank")
    @field:Size(max = 100_000, message = "Text must not exceed 100000 characters")
    val text: String? = null,
    val size: Int = 256,
    val fg: String = "000000",
    val bg: String = "ffffff",
    val format: String = "png",
) {
    /** Non-blank lines of [text], trimmed: one QR code each. */
    val lines: List<String> get() = text.orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() }

    fun forLine(line: String) = QrCodeRequest(text = line, size = size, fg = fg, bg = bg, format = format)
}
