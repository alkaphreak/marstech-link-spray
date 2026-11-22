package fr.marstech.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PasteE2eTest : E2eTest() {

    private fun createPaste(body: String): String {
        val created = postJson("/api/paste", body)
        assertEquals(201, created.statusCode(), created.body())
        return created.json()["id"].asString()
    }

    @Test
    fun shouldCreateAndReadPublicPaste() {
        val id = createPaste("""{"title":"e2e","content":"hello e2e","language":"TEXT","expiration":"ONE_DAY"}""")

        val read = get("/api/paste/$id").json()
        assertEquals("hello e2e", read["content"].asString())
        assertFalse(read["isPasswordProtected"].asBoolean())

        val raw = get("/api/paste/$id/raw")
        assertEquals(200, raw.statusCode())
        assertEquals("hello e2e", raw.body())
        assertTrue(raw.header("Content-Type")!!.startsWith("text/plain"))

        val page = get("/paste/$id")
        assertEquals(200, page.statusCode())
        assertTrue(page.body().contains("hello e2e"))
        assertEquals("hello e2e", get("/paste/$id/raw").body())
    }

    @Test
    fun shouldProtectPasteWithPassword() {
        val id = createPaste("""{"content":"secret e2e","password":"s3cret","expiration":"NEVER"}""")

        assertEquals(401, get("/api/paste/$id").statusCode())
        assertEquals(401, get("/api/paste/$id?password=wrong").statusCode())
        assertEquals(401, get("/api/paste/$id/raw").statusCode())

        val read = get("/api/paste/$id?password=s3cret")
        assertEquals(200, read.statusCode())
        assertEquals("secret e2e", read.json()["content"].asString())
        assertEquals("secret e2e", get("/api/paste/$id/raw?password=s3cret").body())
    }

    @Test
    fun shouldCreatePasteFromForm() {
        val created = postForm("/paste", listOf("inputPasteBinTextArea" to "form e2e"))
        assertEquals(302, created.statusCode())
        val location = created.header("Location")!!
        assertTrue(location.contains("/paste/"), location)

        assertEquals("form e2e", get("/api/paste/${location.substringAfterLast('/')}/raw").body())
    }

    @Test
    fun shouldRejectInvalidPaste() {
        assertEquals(400, postJson("/api/paste", """{"content":""}""").statusCode())
        assertEquals(400, postJson("/api/paste", """{"content":"x","password":"abc"}""").statusCode())
        assertEquals(400, postJson("/api/paste", """{"content":"x","expiration":"FOREVER"}""").statusCode())
        assertEquals(400, postJson("/api/paste", "{not json").statusCode())
    }

    @Test
    fun shouldAnswer404ForUnknownPaste() {
        assertEquals(404, get("/api/paste/unknown-e2e").statusCode())
        assertEquals(404, get("/api/paste/unknown-e2e/raw").statusCode())
        assertEquals(404, get("/paste/unknown-e2e", mapOf("Accept" to "text/html")).statusCode())
        assertEquals(404, get("/paste/unknown-e2e/raw").statusCode())
    }
}
