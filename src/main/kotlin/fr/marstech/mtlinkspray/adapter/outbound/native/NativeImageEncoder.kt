package fr.marstech.mtlinkspray.adapter.outbound.native
import fr.marstech.mtlinkspray.domain.nativetools.ImageEncoder
import fr.marstech.mtlinkspray.domain.nativetools.ImageFormat
import fr.marstech.mtlinkspray.domain.shared.BusyException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

/** `cwebp` for WebP, `caesiumclt` for JPEG and PNG (ADR 0002), each as a subprocess in its own temp directory. */
class NativeImageEncoder(
    private val cwebp: String,
    private val caesium: String,
    private val timeoutSeconds: Long,
) : ImageEncoder {

    override fun encode(
        image: ByteArray, sourceType: String, width: Int, height: Int, format: ImageFormat, quality: Int, maxDimension: Int?,
    ): ByteArray? {
        val dir = Files.createTempDirectory("img")
        try {
            val input = dir.resolve(if (sourceType == "image/png") "in.png" else "in.jpg").apply { writeBytes(image) }
            val output = dir.resolve("out")
            val command = if (format == ImageFormat.WEBP) {
                cwebpCommand(input, output.resolve("in.webp"), quality, width, height, maxDimension)
            } else {
                caesiumCommand(input, output, format.takeIf { it.mediaType != sourceType }, quality, maxDimension)
            }
            Files.createDirectories(output)
            val result = output.resolve("in.${format.extension}")
            return if (run(command, dir) && result.exists()) result.readBytes() else null
        } finally {
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
            throw BusyException("Image encoder is not available", e)
        }
        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor()
            return false
        }
        return process.exitValue() == 0
    }
}
