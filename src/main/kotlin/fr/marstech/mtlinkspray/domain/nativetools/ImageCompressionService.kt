package fr.marstech.mtlinkspray.domain.nativetools
import fr.marstech.mtlinkspray.domain.shared.BusyException
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import fr.marstech.mtlinkspray.domain.shared.PayloadTooLargeException
import fr.marstech.mtlinkspray.domain.shared.ImageTypes
import fr.marstech.mtlinkspray.domain.shared.QuotaPort
import java.io.IOException
import java.io.InputStream
import javax.imageio.ImageIO

enum class ImageFormat(val mediaType: String, val extension: String) {
    WEBP("image/webp", "webp"), JPEG("image/jpeg", "jpg"), PNG("image/png", "png"),
}

class CompressedImage(val bytes: ByteArray, val mediaType: String, val compressed: Boolean)

/**
 * Compresses or converts a PNG or JPEG through an [ImageEncoder] (the native encoders of ADR 0002). The pixel caps
 * bound what the encoder decodes next to the JVM in the 512 MB instance; only the header is read here.
 * Measured 2026-11-04 (aarch64, 512 MB container, idle JVM at 275-300 MB): 7.2 MP to WebP peaks at 314 MB,
 * to JPEG at 338 MB, but to PNG at 503 MB and 2.8 MP to PNG at 368 MB, hence 3 MP for PNG output.
 * A failed or timed-out encode returns the original image, flagged as not compressed.
 */
class ImageCompressionService(
    private val apiKeyResolver: ApiKeyResolver,
    private val permit: NativeWorkPermit,
    private val anonymous: QuotaPort,
    private val encoder: ImageEncoder,
    private val maxInputBytes: Int,
    private val webpMaxPixels: Long,
    private val jpegMaxPixels: Long,
    private val pngMaxPixels: Long,
) {

    fun compress(
        rawApiKey: String?, clientIp: String?, image: InputStream, format: ImageFormat, quality: Int, maxDimension: Int?,
    ): CompressedImage {
        require(quality in 0..100) { "quality must be between 0 and 100" }
        require(maxDimension == null || maxDimension > 0) { "maxDimension must be positive" }
        if (rawApiKey == null) anonymous.count(clientIp ?: "unknown") else apiKeyResolver.resolve(rawApiKey)

        val bytes = image.readNBytes(maxInputBytes + 1)
        if (bytes.size > maxInputBytes) {
            throw PayloadTooLargeException("Image exceeds the maximum allowed size of $maxInputBytes bytes")
        }
        val type = ImageTypes.detect(bytes)
        require(type == "image/png" || type == "image/jpeg") { "Body is not a PNG or JPEG" }
        val (width, height) = requireNotNull(dimensions(bytes)) { "Body is not a readable PNG or JPEG" }
        val maxPixels = when (format) { ImageFormat.WEBP -> webpMaxPixels; ImageFormat.JPEG -> jpegMaxPixels; ImageFormat.PNG -> pngMaxPixels }
        if (width.toLong() * height > maxPixels) {
            throw PayloadTooLargeException("Image has ${width.toLong() * height} pixels, $format output accepts $maxPixels: downscale it")
        }

        if (!permit.tryAcquire()) {
            throw BusyException("Another OCR or image job is running, retry in a few seconds")
        }
        try {
            val encoded = encoder.encode(bytes, type, width, height, format, quality, maxDimension)
            return if (encoded != null) CompressedImage(encoded, format.mediaType, true) else CompressedImage(bytes, type, false)
        } finally {
            permit.release()
        }
    }

    private fun dimensions(bytes: ByteArray): Pair<Int, Int>? =
        ImageIO.createImageInputStream(bytes.inputStream()).use { input ->
            ImageIO.getImageReaders(input).asSequence().firstOrNull()?.let { reader ->
                try {
                    reader.input = input
                    reader.getWidth(0) to reader.getHeight(0)
                } catch (_: IOException) {
                    null
                } finally {
                    reader.dispose()
                }
            }
        }
}
