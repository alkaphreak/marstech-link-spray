package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.OcrResult
import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import fr.marstech.mtlinkspray.utils.ImageTypes
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO

/**
 * Runs the `tesseract` binary as a subprocess (image on stdin, text on stdout) rather than
 * through a JNI binding: no extra dependency, and a native crash cannot take the JVM down.
 * An A4 page at 300 dpi takes about 5 s and 80 MB of RSS per language, 110 MB for fra+eng
 * (tesseract 4.1.1 in the production image, 2026-10-29): with the 512 MB instance only one OCR
 * runs at a time, the API defaults to fra alone, and anonymous callers get a quota.
 * A scanned PDF (MLS-253) is rendered page by page at 300 dpi in grey levels; at about 12 s a page
 * on the prod instance, only [maxPdfPages] pages fit in the timeout, which covers the whole request.
 */
@Service
class OcrServiceImpl(
    private val apiKeyService: ApiKeyService,
    @Value("\${mt.link-spray.ocr.command:tesseract}") private val command: String,
    @Value("\${mt.link-spray.ocr.max-input-bytes:10485760}") private val maxInputBytes: Int,
    @Value("\${mt.link-spray.ocr.timeout-seconds:30}") private val timeoutSeconds: Long,
    @Value("\${mt.link-spray.ocr.anonymous-quota:10}") private val anonymousQuota: Int,
    @Value("\${mt.link-spray.ocr.anonymous-window-ms:3600000}") private val anonymousWindowMillis: Long,
    @Value("\${mt.link-spray.ocr.max-pdf-pages:2}") private val maxPdfPages: Int,
) : OcrService {

    private val running = Semaphore(1)
    private val anonymousCounts = ConcurrentHashMap<String, AtomicInteger>()
    @Volatile
    private var windowStart = System.currentTimeMillis()

    override fun recognize(rawApiKey: String?, clientIp: String?, image: InputStream, lang: String): OcrResult {
        // A "+" left unencoded in the query string arrives as a space
        val languages = lang.trim().replace(' ', '+')
        require(LANGUAGES.matches(languages)) { "Unsupported language '$lang': use fra, eng or fra+eng" }
        if (rawApiKey == null) countAnonymous(clientIp ?: "unknown") else apiKeyService.resolve(rawApiKey)

        val bytes = image.readNBytes(maxInputBytes + 1)
        if (bytes.size > maxInputBytes) {
            throw PayloadTooLargeException("Image exceeds the maximum allowed size of $maxInputBytes bytes")
        }
        val pdf = bytes.size >= PDF_MAGIC.size && bytes.copyOf(PDF_MAGIC.size).contentEquals(PDF_MAGIC)
        require(pdf || ImageTypes.detect(bytes) in SUPPORTED_TYPES) { "Body is not a PNG, JPEG or PDF" }

        (if (pdf) loadPdf(bytes) else null).use { document ->
            if (document != null && document.numberOfPages > maxPdfPages) {
                throw PayloadTooLargeException(
                    "PDF has ${document.numberOfPages} pages, OCR accepts $maxPdfPages: split it or OCR it locally"
                )
            }
            if (!running.tryAcquire()) {
                throw ResponseStatusException(SERVICE_UNAVAILABLE, "Another OCR is running, retry in a few seconds")
            }
            try {
                val start = System.nanoTime()
                val deadline = start + TimeUnit.SECONDS.toNanos(timeoutSeconds)
                val text = if (document == null) runTesseract(bytes, languages, deadline)
                else recognizePages(document, languages, deadline)
                return OcrResult(text, languages, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
            } finally {
                running.release()
            }
        }
    }

    /** One page rendered at a time: a grey A4 page at 300 dpi is about 9 MB, beside tesseract's ~110 MB. */
    private fun recognizePages(document: PDDocument, languages: String, deadline: Long): String {
        val renderer = PDFRenderer(document)
        return (0 until document.numberOfPages).joinToString("\n\n") { page ->
            val image = renderer.renderImageWithDPI(page, PDF_DPI, ImageType.GRAY)
            val png = ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()
            runTesseract(png, languages, deadline)
        }.trim()
    }

    private fun countAnonymous(clientIp: String) {
        val now = System.currentTimeMillis()
        // ponytail: fixed window cleared for every IP at once, sliding window or MLS-161 (Redis) if boundary bursts matter
        if (now - windowStart >= anonymousWindowMillis) {
            anonymousCounts.clear()
            windowStart = now
        }
        if (anonymousCounts.computeIfAbsent(clientIp) { AtomicInteger() }.incrementAndGet() > anonymousQuota) {
            throw ResponseStatusException(
                TOO_MANY_REQUESTS,
                "Anonymous OCR quota of $anonymousQuota reached: retry later or send an X-Api-Key"
            )
        }
    }

    private fun runTesseract(bytes: ByteArray, languages: String, deadline: Long): String {
        val output = Files.createTempFile("ocr", ".txt")
        try {
            val process = try {
                // OpenMP sizes its pool on the host cores, not the 1 vCPU quota: one thread is twice as fast
                // under --cpus=1 (20.5 s vs 11.1 s for an A4 page, 2026-10-29)
                ProcessBuilder(command, "stdin", "stdout", "-l", languages)
                    .apply { environment()["OMP_THREAD_LIMIT"] = "1" }
                    .redirectOutput(output.toFile())
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start()
            } catch (e: IOException) {
                throw ResponseStatusException(SERVICE_UNAVAILABLE, "OCR engine is not available", e)
            }
            try {
                process.outputStream.use { it.write(bytes) }
            } catch (_: IOException) {
                // The process exited before reading the whole image: its exit code says why
            }
            if (!process.waitFor(deadline - System.nanoTime(), TimeUnit.NANOSECONDS)) {
                process.destroyForcibly()
                throw ResponseStatusException(SERVICE_UNAVAILABLE, "OCR took longer than $timeoutSeconds s")
            }
            require(process.exitValue() == 0) { "The OCR engine could not read the image" }
            return Files.readString(output).trim()
        } finally {
            Files.deleteIfExists(output)
        }
    }

    private companion object {
        val SUPPORTED_TYPES = setOf("image/png", "image/jpeg")
        val PDF_MAGIC = "%PDF-".toByteArray()
        const val PDF_DPI = 300f
        // Only the language data installed in the Docker image (tesseract-ocr-fra, -eng)
        val LANGUAGES = Regex("^(fra|eng)(\\+(fra|eng))?$")
    }
}
