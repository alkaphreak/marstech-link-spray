package fr.marstech.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

internal class DashboardE2eTest : E2eTest() {

    private fun slug() = "e2e-" + UUID.randomUUID().toString().take(8)

    private fun dashboard(slug: String? = null, url: String = "https://example.com/item") =
        """{"name":"E2E board","slug":${slug?.let { "\"$it\"" }},"bio":"bio",""" +
            """"items":[{"name":"Example","url":"$url"}]}"""

    @Test
    fun shouldCreateAnonymousDashboardAndServeItsPublicPage() {
        val slug = slug()
        val created = postJson("/api/dashboard", dashboard(slug))
        assertEquals(201, created.statusCode(), created.body())
        val id = created.json()["id"].asString()

        val read = get("/api/dashboard/$id").json()
        assertEquals("E2E board", read["name"].asString())
        assertEquals("https://example.com/item", read["items"][0]["url"].asString())

        assertEquals(200, get("/dashboard/$id").statusCode())
        val page = get("/p/$slug")
        assertEquals(200, page.statusCode())
        assertTrue(page.body().contains("E2E board"))
    }

    @Test
    fun shouldLetOnlyTheOwnerKeyUpdate() {
        val owner = apiKey("dash-owner-${UUID.randomUUID()}")
        val other = apiKey("dash-other-${UUID.randomUUID()}")
        val id = postJson("/api/dashboard", dashboard(), mapOf(owner)).json()["id"].asString()

        val update = """{"id":"$id","name":"Renamed","items":[]}"""
        assertEquals(401, sendJson("PUT", "/api/dashboard/$id", update).statusCode())
        assertEquals(403, sendJson("PUT", "/api/dashboard/$id", update, mapOf(other)).statusCode())

        val updated = sendJson("PUT", "/api/dashboard/$id", update, mapOf(owner))
        assertEquals(200, updated.statusCode(), updated.body())
        assertEquals("Renamed", get("/api/dashboard/$id").json()["name"].asString())
    }

    @Test
    fun shouldRefuseUpdateOfAnonymousDashboard() {
        val id = postJson("/api/dashboard", dashboard()).json()["id"].asString()
        val key = apiKey("dash-anon-${UUID.randomUUID()}")
        assertEquals(403, sendJson("PUT", "/api/dashboard/$id", """{"id":"$id","name":"x"}""", mapOf(key)).statusCode())
    }

    @Test
    fun shouldRejectTakenSlugAndInvalidInput() {
        val slug = slug()
        assertEquals(201, postJson("/api/dashboard", dashboard(slug)).statusCode())
        assertEquals(409, postJson("/api/dashboard", dashboard(slug)).statusCode())

        assertEquals(400, postJson("/api/dashboard", dashboard("Bad Slug!")).statusCode())
        assertEquals(400, postJson("/api/dashboard", dashboard(url = "not-a-url")).statusCode())
        assertEquals(401, postJson("/api/dashboard", dashboard(), mapOf("X-Api-Key" to "nope.nope")).statusCode())
    }

    @Test
    fun shouldCreateDashboardFromForm() {
        val created = postForm("/dashboard", listOf("input-dashboard-name" to "Form board"))
        assertEquals(302, created.statusCode())
        val id = created.header("Location")!!.substringAfterLast('/')
        assertEquals("Form board", get("/api/dashboard/$id").json()["name"].asString())
    }

    @Test
    fun shouldAnswer404ForUnknownDashboard() {
        assertEquals(404, get("/api/dashboard/unknown-e2e").statusCode())
        assertEquals(404, get("/p/unknown-e2e-slug").statusCode())
        assertEquals(404, get("/dashboard/unknown-e2e", mapOf("Accept" to "text/html")).statusCode())
    }
}
