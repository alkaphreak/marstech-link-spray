package fr.marstech.e2e

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test
import java.net.http.HttpRequest
import java.util.UUID

/** Thymeleaf pages, form posts, admin pages and the MCP endpoint. */
internal class WebAndAdminE2eTest : E2eTest() {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/", "/qr", "/spray", "/mailto", "/ocr", "/ip", "/dns", "/cron", "/sleep", "/privacy",
            "/fingerprint", "/uuid", "/random", "/abuse", "/shortener", "/paste", "/dashboard",
        ]
    )
    fun shouldRenderPublicPage(path: String) {
        val page = get(path, mapOf("Accept" to "text/html"))
        assertEquals(200, page.statusCode(), path)
        assertTrue(page.header("Content-Type")!!.startsWith("text/html"), path)
        assertTrue(page.body().contains("</html>"), path)
    }

    @Test
    fun shouldRedirectIndexToRoot() {
        val response = get("/index")
        assertEquals(302, response.statusCode())
        assertTrue(response.header("Location")!!.endsWith("/"))
    }

    @Test
    fun shouldAnswer404ForUnknownPage() {
        assertEquals(404, get("/no-such-page/at-all", mapOf("Accept" to "text/html")).statusCode())
    }

    @Test
    fun shouldShortenAndSprayFromForms() {
        val shortened = postForm("/shortener", listOf("input-link" to "https://example.com/form"))
        assertEquals(200, shortened.statusCode())
        val shortLink = Jsoup.parse(shortened.body()).selectFirst("#shortened-link")!!.attr("value")
        assertEquals("https://example.com/form", redirectOf(shortLink))

        val spray = postForm("/spray", listOf("inputLinkList" to "https://a.example"))
        assertEquals(200, spray.statusCode())
        val sprayLink = Jsoup.parse(spray.body()).selectFirst("p code")!!.text()
        assertTrue(sprayLink.contains("/spray/open?spray="), sprayLink)

        val sprayShortened = postForm("/spray/shorten", listOf("inputLinkList" to "https://b.example"))
        assertEquals(200, sprayShortened.statusCode())
        val shortSpray = Jsoup.parse(sprayShortened.body()).selectFirst("p code")!!.text()
        assertTrue(redirectOf(shortSpray).contains("/spray/open?spray="))

        val open = get("/spray/open?spray=${enc("https://a.example")}")
        assertEquals(200, open.statusCode())
        assertTrue(open.body().contains("https://a.example"))
        assertEquals(400, postForm("/shortener", listOf("input-link" to "not a url")).statusCode())
    }

    @Test
    fun shouldAcceptAbuseReport() {
        assertEquals(200, postForm("/abuse", listOf("inputAbuseDecsription" to "e2e abuse report")).statusCode())
    }

    @Test
    fun shouldRequireAdminCredentialsAndCsrf() {
        assertEquals(401, get("/admin/api-keys").statusCode())
        assertEquals(401, get("/admin/signatures").statusCode())
        assertEquals(401, Admin(password = "wrong").get("/admin/api-keys").statusCode())
        assertEquals(200, Admin().get("/admin/api-keys").statusCode())
        assertEquals(200, Admin().get("/admin/signatures").statusCode())

        val noCsrf = Admin().post("/admin/api-keys", listOf("name" to "x", "namespace" to "no-csrf"), withCsrf = false)
        assertEquals(403, noCsrf.statusCode())
    }

    @Test
    fun shouldChangeKeyRightsThroughAdminLifecycle() {
        val admin = Admin()
        val namespace = "admin-${UUID.randomUUID()}"
        val rawKey = admin.createApiKey(namespace)
        fun works(key: String) = get("/api/checklists/templates", mapOf("X-Api-Key" to key)).statusCode()

        assertEquals(200, works(rawKey))

        val row = Jsoup.parse(admin.get("/admin/api-keys").body())
            .select("tr").first { tr -> tr.select("td").any { it.text() == namespace } }
        val toggle = row.selectFirst("form[action$=/toggle]")!!.attr("action")
        val regenerate = row.selectFirst("form[action$=/regenerate]")!!.attr("action")

        assertEquals(302, admin.post(toggle, listOf("isEnabled" to "false")).statusCode())
        assertEquals(401, works(rawKey))
        assertEquals(302, admin.post(toggle, listOf("isEnabled" to "true")).statusCode())
        assertEquals(200, works(rawKey))

        assertEquals(302, admin.post(regenerate, emptyList()).statusCode())
        val newKey = Jsoup.parse(admin.get("/admin/api-keys").body()).selectFirst("pre")!!.text()
        assertEquals(401, works(rawKey))
        assertEquals(200, works(newKey))

        // Namespace taken
        assertEquals(400, admin.post("/admin/api-keys", listOf("name" to "dup", "namespace" to namespace)).statusCode())
    }

    private fun rpc(body: String) =
        postJson("/mcp", body, mapOf("Accept" to "application/json, text/event-stream"))

    @Test
    fun shouldAnswerStatelessMcpToolCalls() {
        val init = rpc("""{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18",""" +
            """"capabilities":{},"clientInfo":{"name":"e2e","version":"1"}}}""")
        assertEquals(200, init.statusCode(), init.body())
        assertEquals(null, init.header("Mcp-Session-Id"))
        assertEquals("sol4-space", init.json()["result"]["serverInfo"]["name"].asString())

        val tools = rpc("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}""").json()["result"]["tools"]
        assertEquals(
            setOf("uuid", "random_number", "dns_lookup", "dns_reverse", "ip_lookup", "qr_code"),
            (0 until tools.size()).map { tools[it]["name"].asString() }.toSet(),
        )

        val call = rpc("""{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"random_number","arguments":{"min":5,"max":5}}}""")
            .json()["result"]
        assertFalse(call["isError"].asBoolean())
        assertTrue(call["content"][0]["text"].asString().contains("\"value\":5"))

        val invalid = rpc("""{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"dns_lookup","arguments":{"name":"not a domain"}}}""")
            .json()["result"]
        assertTrue(invalid["isError"].asBoolean())
    }

    @Test
    fun shouldRefuseOversizedBodies() {
        // MLS-272: 8 MiB JSON cap on MVC routes, 64 KiB on /mcp whatever the Content-Type
        val json = mapOf("Content-Type" to "application/json", "Accept" to "application/json, text/event-stream")
        fun body(size: Int) = """{"content":"${"a".repeat(size)}"}"""
        fun chunked(text: String) = HttpRequest.BodyPublishers.ofInputStream { text.byteInputStream() }
        val overJson = body(9 * 1024 * 1024)
        val overMcp = body(80 * 1024)

        assertEquals(413, send("POST", "/api/paste", HttpRequest.BodyPublishers.ofString(overJson), json).statusCode())
        // No Content-Length: counted while Jackson reads it
        assertEquals(413, send("POST", "/api/paste", chunked(overJson), json).statusCode())

        assertEquals(413, send("POST", "/mcp", HttpRequest.BodyPublishers.ofString(overMcp), json).statusCode())
        assertEquals(413, send("POST", "/mcp", chunked(overMcp), json).statusCode())
        assertEquals(413, send("POST", "/mcp", chunked(overMcp), json + ("Content-Type" to "text/plain")).statusCode())
    }
}
