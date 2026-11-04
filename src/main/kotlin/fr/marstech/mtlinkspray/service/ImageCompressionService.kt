package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import fr.marstech.mtlinkspray.utils.ImageTypes
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

enum class ImageFormat(val mediaType: String, val extension: String) {
    WEBP("image/webp", "webp"), JPEG("image/jpeg", "jpg"), PNG("image/png", "png"),
}

class CompressedImage(val bytes: ByteArray, val mediaType: String, val compressed: Boolean)

/**
 * Compresses or converts a PNG or JPEG with the native encoders chosen in ADR 0002: `cwebp` for WebP,
 * `caesiumclt` for JPEG and PNG, each as a subprocess like tesseract in [OcrServiceImpl]. The pixel caps
 * bound what the encoder decodes next to the JVM in the 512 MB instance; only the header is read here.
 * Measured 2026-11-04 (aarch64, 512 MB container, idle JVM at 275-300 MB): 7.2 MP to WebP peaks at 314 MB,
 * to JPEG at 338 MB, but to PNG at 503 MB and 2.8 MP to PNG at 368 MB, hence 3 MP for PNG output.
 * A failed or timed-out encode returns the original image, flagged as not compressed.
 */
@Service
class ImageCompressionService(
    private val apiKeyService: ApiKeyService,
    private val permit: NativeWorkPermit,
    @Value("\${mt.link-spray.img.cwebp-command:cwebp}") private val cwebp: String,
    @Value("\${mt.link-spray.img.caesium-command:caesiumclt}") private val caesium: String,
    @Value("\${mt.link-spray.img.max-input-bytes:10485760}") private val maxInputBytes: Int,
    @Value("\${mt.link-spray.img.timeout-seconds:30}") private val timeoutSeconds: Long,
    @Value("\${mt.link-spray.img.webp-max-pixels:8000000}") private val webpMaxPixels: Long,
    @Value("\${mt.link-spray.img.jpeg-max-pixels:8000000}") private val jpegMaxPixels: Long,
    @Value("\${mt.link-spray.img.png-max-pixels:3000000}") private val pngMaxPixels: Long,
    @Value("\${mt.link-spray.img.anonymous-quota:20}") anonymousQuota: Int,
    @Value("\${mt.link-spray.img.anonymous-window-ms:3600000}") anonymousWindowMillis: Long,
) {

    private val anonymous = AnonymousQuota("image compression", anonymousQuota, anonymousWindowMillis)

    fun compress(
        rawApiKey: String?, clientIp: String?, image: InputStream, format: ImageFormat, quality: Int, maxDimension: Int?,
    ): CompressedImage {
        require(quality in 0..100) { "quality must be between 0 and 100" }
        require(maxDimension == null || maxDimension > 0) { "maxDimension must be positive" }
        if (rawApiKey == null) anonymous.count(clientIp ?: "unknown") else apiKeyService.resolve(rawApiKey)

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
            throw ResponseStatusException(SERVICE_UNAVAILABLE, "Another OCR or image job is running, retry in a few seconds")
        }
        val dir = Files.createTempDirectory("img")
        try {
            val input = dir.resolve(if (type == "image/png") "in.png" else "in.jpg").apply { writeBytes(bytes) }
            val output = dir.resolve("out")
            val command = if (format == ImageFormat.WEBP) {
                cwebpCommand(input, output.resolve("in.webp"), quality, width, height, maxDimension)
            } else {
                caesiumCommand(input, output, format.takeIf { it.mediaType != type }, quality, maxDimension)
            }
            Files.createDirectories(output)
            val result = output.resolve("in.${format.extension}")
            return if (run(command, dir) && result.exists()) CompressedImage(result.readBytes(), format.mediaType, true)
            else CompressedImage(bytes, type, false)
        } finally {
            permit.release()
            @OptIn(kotlin.io.path.ExperimentalPathApi::class)
            dir.deleteRecursively()
        }
    }

    // ponytail: cwebp ignores EXIF orientation, a rotated phone photo comes out sideways; pre-rotate if users report it
    private fun cwebpCommand(input: Path, output: Path, quality: Int, width: Int, height: Int, maxDimension: Int?): List<String> {
        val resize = maxDimension?.takeIf { maxOf(width, height) > it }?.let {
            if (width >= height) listOf("-resize", "$it", "0") else listOf("-resize", "0", "$it")
        }.orEmpty()
        return listOf(cwebp, "-quiet", "-q", "$quality", "-metadata", "none") + resize + listOf(input.toString(), "-o", output.toString())
    }

    /** [convertTo] is null for a same-format compression: caesiumclt rejects `--format` equal to the input's, with exit code 0. */
    private fun caesiumCommand(input: Path, output: Path, convertTo: ImageFormat?, quality: Int, maxDimension: Int?): List<String> {
        val resize = maxDimension?.let { listOf("--long-edge", "$it", "--no-upscale") }.orEmpty()
        val convert = convertTo?.let { listOf("--format", it.name.lowercase()) }.orEmpty()
        return listOf(caesium, "-Q", "--threads", "1", "--keep-orientation", "-q", "$quality") + convert +
            resize + listOf("-o", output.toString(), input.toString())
    }

    private fun run(command: List<String>, dir: Path): Boolean {
        val process = try {
            ProcessBuilder(command).directory(dir.toFile())
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
        } catch (e: IOException) {
            throw ResponseStatusException(SERVICE_UNAVAILABLE, "Image encoder is not available", e)
        }
        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor()
            return false
        }
        return process.exitValue() == 0
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
