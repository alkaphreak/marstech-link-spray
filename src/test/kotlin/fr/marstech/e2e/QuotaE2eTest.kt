package fr.marstech.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/** Per-IP quotas (ADR 0006) with small buckets; every request comes from 127.0.0.1. Own context. */
@TestPropertySource(
    properties = [
        "mt.link-spray.rate-limit.writes.quota=3",
        "mt.link-spray.rate-limit.writes.window-ms=600000",
        "mt.link-spray.rate-limit.abuse.quota=1",
        "mt.link-spray.rate-limit.abuse.window-ms=3600000",
        "mt.link-spray.ocr.anonymous-quota=1",
        "mt.link-spray.img.anonymous-quota=1",
        // Missing binaries here, so the 503 path stays covered
        "mt.link-spray.ocr.command=/nonexistent/tesseract",
        "mt.link-spray.img.cwebp-command=/nonexistent/cwebp",
        "mt.link-spray.img.caesium-command=/nonexistent/caesiumclt",
    ]
)
internal class QuotaE2eTest : E2eTest() {

    private val png = ByteArrayOutputStream()
        .also { ImageIO.write(BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "png", it) }.toByteArray()

    @Test
    fun shouldShareOneWriteBucketPerIp() {
        // A shortened spray of 3 URLs costs 4 tokens, more than the whole bucket: 400, nothing charged
        val urls = (1..3).joinToString("&") { "inputLinkList=${enc("https://q$it.example")}" }
        assertEquals(400, get("/api/spray?shorten=true&$urls").statusCode())

        assertEquals(201, postJson("/api/paste", """{"content":"q1"}""").statusCode())
        assertEquals(201, postJson("/api/url-shortener", """{"url":"https://example.com/q2"}""").statusCode())
        assertEquals(302, postForm("/paste", listOf("inputPasteBinTextArea" to "q3")).statusCode())

        val limited = postJson("/api/paste", """{"content":"q4"}""")
        assertEquals(429, limited.statusCode())
        assertNotNull(limited.header("Retry-After"))
        assertEquals(429, get("/api/url-shortener/shorten?url=${enc("https://example.com/q5")}").statusCode())
    }

    @Test
    fun shouldLimitAbuseReports() {
        assertEquals(200, postForm("/abuse", listOf("inputAbuseDecsription" to "first")).statusCode())
        assertEquals(429, postForm("/abuse", listOf("inputAbuseDecsription" to "second")).statusCode())
    }

    @Test
    fun shouldLimitAnonymousNativeTools() {
        // First anonymous call passes the quota (then 503: no binary), the second is refused
        assertEquals(503, postRaw("/api/ocr", png, "image/png").statusCode())
        assertEquals(429, postRaw("/api/ocr", png, "image/png").statusCode())
        assertEquals(503, postRaw("/api/img/compress", png, "image/png").statusCode())
        assertEquals(429, postRaw("/api/img/compress", png, "image/png").statusCode())
    }
}
