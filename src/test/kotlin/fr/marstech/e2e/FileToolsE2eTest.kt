package fr.marstech.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.http.HttpRequest
import java.util.UUID
import javax.imageio.ImageIO

/**
 * Images (on FakeS3), OCR and compression (on the e2e-bin stand-in scripts) and document conversion.
 */
internal class FileToolsE2eTest : E2eTest() {

    private val auth by lazy { mapOf(apiKey("files-${UUID.randomUUID()}")) }

    private fun png(width: Int = 8, height: Int = 8): ByteArray =
        ByteArrayOutputStream().also { ImageIO.write(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", it) }
            .toByteArray()

    private fun multipart(bytes: ByteArray, filename: String, contentType: String): Pair<HttpRequest.BodyPublisher, String> {
        val boundary = "e2e-" + UUID.randomUUID()
        val head = "--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"$filename\"\r\n" +
            "Content-Type: $contentType\r\n\r\n"
        val body = head.toByteArray() + bytes + "\r\n--$boundary--\r\n".toByteArray()
        return HttpRequest.BodyPublishers.ofByteArray(body) to "multipart/form-data; boundary=$boundary"
    }

    private fun upload(bytes: ByteArray, isPrivate: Boolean, headers: Map<String, String> = auth): java.net.http.HttpResponse<String> {
        val (body, type) = multipart(bytes, "e2e.png", "image/png")
        return send("POST", "/api/images?isPrivate=$isPrivate", body, headers + ("Content-Type" to type))
    }

    @Test
    fun shouldStoreServeAndDeleteImages() {
        val image = png()
        val created = upload(image, isPrivate = false)
        assertEquals(201, created.statusCode(), created.body())
        val id = created.json()["id"].asString()
        assertEquals("image/png", created.json()["contentType"].asString())

        val anonymous = sendBytes("GET", "/api/images/$id")
        assertEquals(200, anonymous.statusCode())
        assertTrue(image.contentEquals(anonymous.body()))
        assertEquals(1, get("/api/images", auth).json().size())

        val privateId = upload(image, isPrivate = true).json()["id"].asString()
        assertEquals(401, get("/api/images/$privateId").statusCode())
        val other = mapOf(apiKey("files-other-${UUID.randomUUID()}"))
        assertEquals(403, get("/api/images/$privateId", other).statusCode())
        assertEquals(200, get("/api/images/$privateId", auth).statusCode())
        assertEquals(403, delete("/api/images/$privateId", other).statusCode())

        assertEquals(204, delete("/api/images/$id", auth).statusCode())
        assertEquals(404, get("/api/images/$id").statusCode())
    }

    @Test
    fun shouldRejectInvalidImageUploads() {
        assertEquals(400, upload("not an image".toByteArray(), isPrivate = false).statusCode())
        assertEquals(401, upload(png(), isPrivate = false, headers = emptyMap()).statusCode())
        assertEquals(401, upload(png(), isPrivate = false, headers = mapOf("X-Api-Key" to "nope.nope")).statusCode())
        assertEquals(401, get("/api/images", mapOf("X-Api-Key" to "nope.nope")).statusCode())
        assertEquals(404, get("/api/images/unknown", auth).statusCode())
        assertEquals(404, delete("/api/images/unknown", auth).statusCode())
        assertEquals(401, delete("/api/images/unknown", mapOf("X-Api-Key" to "nope.nope")).statusCode())
    }

    @Test
    fun shouldRecognizeTextThroughTheOcrEngine() {
        val result = postRaw("/api/ocr?lang=eng", png(), "image/png", auth)
        assertEquals(200, result.statusCode(), result.body())
        assertEquals("E2E OCR TEXT", result.json()["text"].asString().trim())
        assertEquals("eng", result.json()["lang"].asString())

        assertEquals(400, postRaw("/api/ocr?lang=deu", png(), "image/png", auth).statusCode())
        assertEquals(400, postRaw("/api/ocr", "not an image".toByteArray(), "image/png", auth).statusCode())
        assertEquals(415, postRaw("/api/ocr", png(), "text/plain", auth).statusCode())
        assertEquals(401, postRaw("/api/ocr", png(), "image/png", mapOf("X-Api-Key" to "nope.nope")).statusCode())
    }

    @Test
    fun shouldCompressThroughTheEncoders() {
        val webp = sendBytes("POST", "/api/img/compress?format=webp",
            HttpRequest.BodyPublishers.ofByteArray(png()), auth + ("Content-Type" to "image/png"))
        assertEquals(200, webp.statusCode())
        assertEquals("image/webp", webp.headers().firstValue("Content-Type").orElse(null))
        assertEquals("true", webp.headers().firstValue("X-Image-Compressed").orElse(null))
        assertEquals("E2E-WEBP", String(webp.body()))

        val same = sendBytes("POST", "/api/img/compress?format=png",
            HttpRequest.BodyPublishers.ofByteArray(png()), auth + ("Content-Type" to "image/png"))
        assertEquals(200, same.statusCode())
        assertEquals("image/png", same.headers().firstValue("Content-Type").orElse(null))

        assertEquals(400, postRaw("/api/img/compress?quality=101", png(), "image/png", auth).statusCode())
        assertEquals(400, postRaw("/api/img/compress", "not an image".toByteArray(), "image/png", auth).statusCode())
        assertEquals(413, postRaw("/api/img/compress?format=png", png(2000, 2000), "image/png", auth).statusCode())
        assertEquals(401, postRaw("/api/img/compress", png(), "image/png", mapOf("X-Api-Key" to "nope.nope")).statusCode())
    }

    @Test
    fun shouldRoundTripMarkdownThroughPdf() {
        val pdf = sendBytes("POST", "/api/md2pdf",
            HttpRequest.BodyPublishers.ofString("# E2E title\n\nSome paragraph."),
            auth + ("Content-Type" to "text/markdown"))
        assertEquals(200, pdf.statusCode())
        assertEquals("application/pdf", pdf.headers().firstValue("Content-Type").orElse(null))
        assertTrue(String(pdf.body().copyOf(5)) == "%PDF-")

        val markdown = postRaw("/api/pdf2md", pdf.body(), "application/pdf", auth)
        assertEquals(200, markdown.statusCode(), markdown.body())
        assertTrue(markdown.body().contains("E2E title"), markdown.body())
    }

    @Test
    fun shouldRejectBadDocumentInput() {
        assertEquals(401, postRaw("/api/md2pdf", "x".toByteArray(), "text/markdown").statusCode())
        assertEquals(401, postRaw("/api/md2pdf", "x".toByteArray(), "text/markdown", mapOf("X-Api-Key" to "nope.nope")).statusCode())
        assertEquals(400, postRaw("/api/md2pdf", ByteArray(0), "text/markdown", auth).statusCode())
        assertEquals(413, postRaw("/api/md2pdf", ByteArray(1_100_000) { 'a'.code.toByte() }, "text/markdown", auth).statusCode())
        assertEquals(415, postRaw("/api/md2pdf", "x".toByteArray(), "application/json", auth).statusCode())
        assertEquals(400, postRaw("/api/pdf2md", "not a pdf".toByteArray(), "application/pdf", auth).statusCode())
    }
}
