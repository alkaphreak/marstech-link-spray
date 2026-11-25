package fr.marstech.mtlinkspray.domain.shared

/**
 * Raster image types accepted by the image storage API (MLS-203), detected from the file's magic
 * bytes rather than trusted from the client-supplied Content-Type. SVG is deliberately excluded:
 * it can carry script and would be rendered inline under the MLS origin (stored XSS).
 */
enum class ImageType(val mediaType: String) {
    JPEG("image/jpeg"), PNG("image/png"), GIF("image/gif"), WEBP("image/webp");

    companion object {
        val MEDIA_TYPES: Set<String> = entries.map { it.mediaType }.toSet()

        fun detect(bytes: ByteArray): ImageType? = when {
            bytes.startsWith(0xFF, 0xD8, 0xFF) -> JPEG
            bytes.startsWith(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) -> PNG
            bytes.startsWith(0x47, 0x49, 0x46, 0x38) -> GIF
            bytes.startsWith(0x52, 0x49, 0x46, 0x46) && bytes.size >= 12 &&
                bytes.copyOfRange(8, 12).contentEquals("WEBP".toByteArray()) -> WEBP
            else -> null
        }

        private fun ByteArray.startsWith(vararg prefix: Int): Boolean =
            size >= prefix.size && prefix.indices.all { this[it] == prefix[it].toByte() }
    }
}
