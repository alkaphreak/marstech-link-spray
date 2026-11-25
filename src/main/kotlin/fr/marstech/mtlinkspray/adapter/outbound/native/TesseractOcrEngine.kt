package fr.marstech.mtlinkspray.adapter.outbound.native
import fr.marstech.mtlinkspray.domain.nativetools.OcrEngine
import fr.marstech.mtlinkspray.domain.shared.BusyException
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.TimeUnit

/**
 * Runs the `tesseract` binary as a subprocess (image on stdin, text on stdout) rather than
 * through a JNI binding: no extra dependency, and a native crash cannot take the JVM down.
 */
class TesseractOcrEngine(private val command: String) : OcrEngine {

    override fun recognize(image: ByteArray, languages: String, deadlineNanos: Long): String? {
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
                throw BusyException("OCR engine is not available", e)
            }
            try {
                process.outputStream.use { it.write(image) }
            } catch (_: IOException) {
                // The process exited before reading the whole image: its exit code says why
            }
            if (!process.waitFor(deadlineNanos - System.nanoTime(), TimeUnit.NANOSECONDS)) {
                process.destroyForcibly()
                return null
            }
            require(process.exitValue() == 0) { "The OCR engine could not read the image" }
            return Files.readString(output).trim()
        } finally {
            Files.deleteIfExists(output)
        }
    }
}
