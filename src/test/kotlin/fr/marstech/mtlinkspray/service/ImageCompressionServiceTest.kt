package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.mock
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImageCompressionServiceTest {

    @TempDir
    lateinit var dir: Path

    private val png = ByteArrayOutputStream().also {
        ImageIO.write(BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB), "png", it)
    }.toByteArray()

    /** A stand-in encoder: writes [content] to the path after `-o` (a file for cwebp, `<dir>/in.png` for caesiumclt). */
    private fun fakeEncoder(content: String = "encoded", target: String = "\$out", extra: String = ""): String =
        dir.resolve("enc-${System.nanoTime()}.sh").also {
            Files.writeString(it, "#!/bin/sh\n$extra\nwhile [ \$# -gt 0 ]; do [ \"\$1\" = -o ] && out=\$2; shift; done\nprintf '$content' > \"$target\"\n")
            it.toFile().setExecutable(true)
        }.toString()

    private fun service(
        cwebp: String = fakeEncoder(),
        caesium: String = fakeEncoder(target = "\$out/in.png"),
        permit: NativeWorkPermit = NativeWorkPermit(),
        maxPixels: Long = 1_000,
        timeoutSeconds: Long = 5,
    ) = ImageCompressionService(mock(), permit, cwebp, caesium, 1_000_000, timeoutSeconds, maxPixels, maxPixels, maxPixels, 10, 3_600_000)

    private fun ImageCompressionService.compress(format: ImageFormat, bytes: ByteArray = png) =
        compress(null, "1.2.3.4", bytes.inputStream(), format, 75, null)

    @Test
    fun `webp output comes from cwebp`() {
        val result = service().compress(ImageFormat.WEBP)
        assertTrue(result.compressed)
        assertEquals("image/webp", result.mediaType)
        assertEquals("encoded", String(result.bytes))
    }

    @Test
    fun `png output comes from caesiumclt`() {
        val result = service().compress(ImageFormat.PNG)
        assertTrue(result.compressed)
        assertEquals("image/png", result.mediaType)
    }

    /** A stand-in encoder that records its arguments in [log] and writes like [fakeEncoder]. */
    private fun recordingEncoder(log: Path, target: String) = fakeEncoder(target = target, extra = "echo \"\$@\" > '$log'")

    @Test
    fun `maxDimension resizes the long edge with cwebp`() {
        val log = dir.resolve("args")
        service(cwebp = recordingEncoder(log, "\$out")).compress(null, null, png.inputStream(), ImageFormat.WEBP, 75, 10)
        assertTrue(Files.readString(log).contains("-resize 10 0"))
    }

    @Test
    fun `caesiumclt converts only across formats and resizes the long edge`() {
        val log = dir.resolve("args")
        service(caesium = recordingEncoder(log, "\$out/in.jpg")).compress(null, null, png.inputStream(), ImageFormat.JPEG, 75, 10)
        assertTrue(Files.readString(log).contains("--format jpeg --long-edge 10"))
        service(caesium = recordingEncoder(log, "\$out/in.png")).compress(ImageFormat.PNG)
        assertFalse(Files.readString(log).contains("--format"))
    }

    @Test
    fun `out-of-range quality and maxDimension are rejected`() {
        assertThrows<IllegalArgumentException> { service().compress(null, null, png.inputStream(), ImageFormat.WEBP, 101, null) }
        assertThrows<IllegalArgumentException> { service().compress(null, null, png.inputStream(), ImageFormat.WEBP, 75, 0) }
    }

    @Test
    fun `a missing encoder binary returns 503`() {
        val e = assertThrows<ResponseStatusException> { service(cwebp = dir.resolve("missing").toString()).compress(ImageFormat.WEBP) }
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.statusCode)
    }

    @Test
    fun `a failing encoder returns the original image`() {
        val result = service(cwebp = fakeEncoder(extra = "exit 1")).compress(ImageFormat.WEBP)
        assertFalse(result.compressed)
        assertEquals("image/png", result.mediaType)
        assertContentEquals(png, result.bytes)
    }

    @Test
    fun `a timed-out encoder returns the original image`() {
        val result = service(cwebp = fakeEncoder(extra = "sleep 5"), timeoutSeconds = 1).compress(ImageFormat.WEBP)
        assertFalse(result.compressed)
    }

    @Test
    fun `an image over the pixel cap is rejected before encoding`() {
        assertThrows<PayloadTooLargeException> { service(maxPixels = 799).compress(ImageFormat.WEBP) }
    }

    @Test
    fun `a body that is not a png or jpeg is rejected`() {
        assertThrows<IllegalArgumentException> { service().compress(ImageFormat.WEBP, "GIF89a....".toByteArray()) }
    }

    @Test
    fun `a busy permit returns 503 and the permit is released after a job`() {
        val permit = NativeWorkPermit()
        service(permit = permit).compress(ImageFormat.WEBP)
        assertEquals(1, permit.availablePermits())
        permit.acquire()
        val e = assertThrows<ResponseStatusException> { service(permit = permit).compress(ImageFormat.WEBP) }
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.statusCode)
    }
}
