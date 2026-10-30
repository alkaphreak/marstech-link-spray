package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.test.assertContains
import kotlin.test.assertEquals

class OcrServiceImplTest {

    @TempDir
    lateinit var dir: Path

    private val apiKeyService: ApiKeyService = mock()

    private val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) + "pixels".toByteArray()

    /** A stand-in for tesseract: a shell script that receives the same arguments ($4 is the language). */
    private fun fakeEngine(body: String): String = dir.resolve("engine-${System.nanoTime()}.sh").also {
        Files.writeString(it, "#!/bin/sh\n$body\n")
        it.toFile().setExecutable(true)
    }.toString()

    private val echoLanguage = "cat > /dev/null; printf ' text in %s \\f' \"\$4\""

    private fun service(
        command: String = fakeEngine(echoLanguage),
        maxInputBytes: Int = 1024,
        timeoutSeconds: Long = 5,
        anonymousQuota: Int = 10,
        anonymousWindowMillis: Long = 3_600_000,
    ) = OcrServiceImpl(apiKeyService, command, maxInputBytes, timeoutSeconds, anonymousQuota, anonymousWindowMillis)

    private fun OcrServiceImpl.anonymous(image: ByteArray = png, lang: String = "fra+eng") =
        recognize(null, "203.0.113.7", image.inputStream(), lang)

    @Test
    fun shouldReturnTrimmedEngineOutputAndLanguage() {
        // When
        val result = service().anonymous()

        // Then
        assertEquals("text in fra+eng", result.text)
        assertEquals("fra+eng", result.lang)
    }

    @Test
    fun shouldRunEngineWithSingleOpenMpThread() {
        // When
        val result = service(command = fakeEngine("cat > /dev/null; printf %s \"\$OMP_THREAD_LIMIT\"")).anonymous()

        // Then
        assertEquals("1", result.text)
    }

    @Test
    fun shouldTreatSpaceAsPlusInLanguage() {
        // When: "fra+eng" sent unencoded in a query string is decoded as "fra eng"
        val result = service().anonymous(lang = "fra eng")

        // Then
        assertEquals("fra+eng", result.lang)
    }

    @Test
    fun shouldRejectUnsupportedLanguage() {
        // When / Then
        assertThrows<IllegalArgumentException> { service().anonymous(lang = "deu; rm -rf /") }
    }

    @Test
    fun shouldRejectBodyThatIsNotPngOrJpeg() {
        // When / Then
        assertThrows<IllegalArgumentException> { service().anonymous(image = "GIF89a".toByteArray()) }
    }

    @Test
    fun shouldRejectImageAboveMaxSize() {
        // When / Then
        assertThrows<PayloadTooLargeException> { service(maxInputBytes = 8).anonymous() }
    }

    @Test
    fun shouldReturnTooManyRequestsWhenAnonymousQuotaIsSpent() {
        // Given
        val service = service(anonymousQuota = 2)
        repeat(2) { service.anonymous() }

        // When
        val e = assertThrows<ResponseStatusException> { service.anonymous() }

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.statusCode)
    }

    @Test
    fun shouldResetAnonymousQuotaWhenWindowEnds() {
        // Given: a zero-length window restarts on every call
        val service = service(anonymousQuota = 1, anonymousWindowMillis = 0)

        // When / Then
        repeat(3) { service.anonymous() }
    }

    @Test
    fun shouldSkipQuotaWithValidApiKey() {
        // Given
        val service = service(anonymousQuota = 1)

        // When
        repeat(3) { service.recognize("key.secret", "203.0.113.7", png.inputStream(), "eng") }

        // Then
        verify(apiKeyService, times(3)).resolve("key.secret")
    }

    @Test
    fun shouldRejectInvalidApiKeyBeforeReadingImage() {
        // Given
        doThrow(IllegalAccessException("Invalid API key")).whenever(apiKeyService).resolve("bad")
        val image = png.inputStream()

        // When / Then
        assertThrows<IllegalAccessException> { service().recognize("bad", null, image, "eng") }
        assertEquals(png.size, image.available())
    }

    @Test
    fun shouldReturnServiceUnavailableWhenEngineIsMissing() {
        // When
        val e = assertThrows<ResponseStatusException> { service(command = dir.resolve("absent").toString()).anonymous() }

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.statusCode)
    }

    @Test
    fun shouldReturnBadRequestWhenEngineFails() {
        // When / Then
        assertThrows<IllegalArgumentException> { service(command = fakeEngine("exit 1")).anonymous() }
    }

    @Test
    fun shouldReturnServiceUnavailableWhenEngineIsTooSlow() {
        // When
        val e = assertThrows<ResponseStatusException> {
            service(command = fakeEngine("sleep 5"), timeoutSeconds = 1).anonymous()
        }

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.statusCode)
    }

    @Test
    fun shouldReturnServiceUnavailableWhileAnotherOcrRuns() {
        // Given: the first call holds the engine until the test removes the gate file
        val started = dir.resolve("started")
        val gate = dir.resolve("gate").also { Files.createFile(it) }
        val service = service(command = fakeEngine("cat > /dev/null; touch $started; while [ -f $gate ]; do sleep 0.05; done"))
        val first = CompletableFuture.supplyAsync { service.anonymous() }
        while (!started.exists()) Thread.sleep(20)

        // When
        val e = assertThrows<ResponseStatusException> { service.anonymous() }
        Files.delete(gate)

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.statusCode)
        first.get()
        verify(apiKeyService, never()).resolve(org.mockito.kotlin.any())
    }

    @Test
    fun shouldRecognizeTextWithRealTesseract() {
        // Given: only where tesseract is installed (the Docker image, a dev Mac), skipped elsewhere
        val engine = listOf("/usr/bin/tesseract", "/usr/local/bin/tesseract", "/opt/homebrew/bin/tesseract")
            .firstOrNull { Path.of(it).exists() }
        assumeTrue(engine != null, "tesseract not installed")
        val image = BufferedImage(900, 120, BufferedImage.TYPE_INT_RGB).apply {
            createGraphics().run {
                color = Color.WHITE
                fillRect(0, 0, width, height)
                color = Color.BLACK
                font = Font(Font.SANS_SERIF, Font.PLAIN, 48)
                drawString("Hello OCR world", 30, 80)
                dispose()
            }
        }
        val bytes = ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()

        // When
        val result = service(command = engine!!, maxInputBytes = 1024 * 1024, timeoutSeconds = 30)
            .anonymous(image = bytes, lang = "eng")

        // Then
        assertContains(result.text, "Hello OCR world")
    }
}
