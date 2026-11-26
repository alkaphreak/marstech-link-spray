package fr.marstech.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ShortenerE2eTest : E2eTest() {

    private fun createShortLink(target: String): String {
        val created = postJson("/api/url-shortener", """{"url":"$target"}""")
        assertEquals(201, created.statusCode(), created.body())
        return created.header("Location")!!.substringAfterLast('/')
    }

    @Test
    fun shouldCreateRedirectAndCountClicks() {
        val uid = createShortLink("https://example.com/e2e")

        val redirect = get("/$uid", mapOf("User-Agent" to "Mozilla/5.0 (Macintosh) Firefox/140.0"))
        assertEquals(302, redirect.statusCode())
        assertEquals("https://example.com/e2e", redirect.header("Location"))

        val stats = get("/api/url-shortener/$uid/stats").json()
        assertEquals(1, stats["clickCount"].asInt())
        assertEquals(0, stats["botClickCount"].asInt())
    }

    @Test
    fun shouldAnswer303BehindTheProxy() {
        // Behind DO's proxy (X-Forwarded-*), Boot 4.2's relative redirects answer 303 See Other (MLS-282)
        val uid = createShortLink("https://example.com/proxied")

        val redirect = get("/$uid", mapOf("X-Forwarded-Proto" to "https", "X-Forwarded-Host" to "sol4.space"))
        assertEquals(303, redirect.statusCode())
        assertEquals("https://example.com/proxied", redirect.header("Location"))
    }

    @Test
    fun shouldCountHeadAsBotClick() {
        val uid = createShortLink("https://example.com/head")

        assertEquals(302, send("HEAD", "/$uid").statusCode())

        val stats = get("/api/url-shortener/$uid/stats").json()
        assertEquals(0, stats["clickCount"].asInt())
        assertEquals(1, stats["botClickCount"].asInt())
    }

    /** Behind the App Platform proxy: short URLs must carry the public scheme and host (framework strategy). */
    @Test
    fun shouldBuildShortUrlFromForwardedHeaders() {
        val response = get(
            "/api/url-shortener/shorten?url=${enc("https://example.com/forwarded")}",
            mapOf("X-Forwarded-Proto" to "https", "X-Forwarded-Host" to "sol4.space"),
        )
        assertEquals(200, response.statusCode(), response.body())
        assertTrue(response.body().trim().startsWith("https://sol4.space/"), response.body())
    }

    @Test
    fun shouldShortenThroughGet() {
        val response = get("/api/url-shortener/shorten?url=${enc("https://example.com/get")}")
        assertEquals(200, response.statusCode(), response.body())
        assertEquals("https://example.com/get", redirectOf(response.body()))
    }

    @Test
    fun shouldRejectInvalidUrl() {
        assertEquals(400, postJson("/api/url-shortener", """{"url":"not a url"}""").statusCode())
        assertEquals(400, get("/api/url-shortener/shorten?url=${enc("ftp//nope")}").statusCode())
    }

    @Test
    fun shouldAnswer404ForUnknownCode() {
        assertEquals(404, get("/api/url-shortener/zzzzzzzz/stats").statusCode())
        assertEquals(404, get("/zzzzzzzz").statusCode())
    }
}
