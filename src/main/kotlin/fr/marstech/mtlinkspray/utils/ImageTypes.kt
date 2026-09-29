package fr.marstech.mtlinkspray.utils

/**
 * Raster image types accepted by the image storage API (MLS-203), detected from the file's magic
 * bytes rather than trusted from the client-supplied Content-Type. SVG is deliberately excluded:
 * it can carry script and would be rendered inline under the MLS origin (stored XSS).
 */
object ImageTypes {

    val ALLOWED: Set<String> = setOf("image/jpeg", "image/png", "image/gif", "image/webp")

    fun detect(bytes: ByteArray): String? = when {
        bytes.startsWith(0xFF, 0xD8, 0xFF) -> "image/jpeg"
        bytes.startsWith(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) -> "image/png"
        bytes.startsWith(0x47, 0x49, 0x46, 0x38) -> "image/gif"
        bytes.startsWith(0x52, 0x49, 0x46, 0x46) && bytes.size >= 12 &&
            bytes.copyOfRange(8, 12).contentEquals("WEBP".toByteArray()) -> "image/webp"
        else -> null
    }

    private fun ByteArray.startsWith(vararg prefix: Int): Boolean =
        size >= prefix.size && prefix.indices.all { this[it] == prefix[it].toByte() }
}
