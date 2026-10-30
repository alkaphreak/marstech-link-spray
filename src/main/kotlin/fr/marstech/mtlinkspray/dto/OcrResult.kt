package fr.marstech.mtlinkspray.dto

/**
 * Text recognized in an image by the OCR API (MLS-252), with the Tesseract language(s) used.
 */
data class OcrResult(
    val text: String,
    val lang: String,
    val processingTimeMs: Long,
)
