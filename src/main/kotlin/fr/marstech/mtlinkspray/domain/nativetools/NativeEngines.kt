package fr.marstech.mtlinkspray.domain.nativetools

/** The OCR engine (tesseract in ADR 0002). Throws [fr.marstech.mtlinkspray.domain.shared.BusyException] when it is missing. */
interface OcrEngine {
    /**
     * Text of one PNG or JPEG in [languages] (`fra`, `eng`, `fra+eng`), or null when [deadlineNanos]
     * ([System.nanoTime]) passes first. Throws [IllegalArgumentException] when the engine cannot read the image.
     */
    fun recognize(image: ByteArray, languages: String, deadlineNanos: Long): String?
}

/** The image encoders (cwebp and caesiumclt in ADR 0002). Throws [fr.marstech.mtlinkspray.domain.shared.BusyException] when one is missing. */
interface ImageEncoder {
    /**
     * [image] ([sourceType] `image/png` or `image/jpeg`, [width] x [height]) encoded as [format], or null when the
     * encoder fails, times out or writes nothing. [maxDimension] caps the longest edge without upscaling.
     */
    fun encode(image: ByteArray, sourceType: String, width: Int, height: Int, format: ImageFormat, quality: Int, maxDimension: Int?): ByteArray?
}
