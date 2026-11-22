package fr.marstech.e2e

import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.TestPropertySource
import java.util.UUID

/**
 * Documents as the application stores them today (captured 2026-11-21, v0.16.0), `_class` values
 * included, must stay readable: ADR 0007 moves every one of these classes. Own context, so the
 * polymorphic dashboard item is read on a cold mapping context, before the app writes one.
 */
@TestPropertySource(properties = ["e2e.context=mongo-compatibility"])
internal class MongoCompatibilityE2eTest : E2eTest() {

    private fun insert(collection: String, json: String) {
        mongo.getCollection(collection).insertOne(Document.parse(json))
    }

    @Test
    fun shouldReadLegacyDocuments() {
        val suffix = UUID.randomUUID().toString().take(8)
        val namespace = "legacy-$suffix"

        // A key holder from before the migration must still authenticate
        insert("api_keys", """
            {"_id": "legacy-key-$suffix", "name": "legacy", "keyId": "lgk$suffix",
             "secretHash": "${BCryptPasswordEncoder().encode("legacy-secret")}", "namespace": "$namespace",
             "creationDate": {"${'$'}date": "2026-11-22T01:49:32.400Z"}, "isEnabled": true,
             "_class": "fr.marstech.mtlinkspray.entity.ApiKeyEntity"}""")
        val auth = "X-Api-Key" to "lgk$suffix.legacy-secret"

        insert("mt-link-spray-dashboard-entity", """
            {"_id": "legacy-dash-$suffix", "creationDate": {"${'$'}date": "2026-11-22T01:49:32.517Z"}, "isEnabled": true,
             "metadata": {}, "author": {"dateTime": {"${'$'}date": "2026-11-22T01:49:32.517Z"}}, "historyItems": [],
             "name": "Legacy board", "items": [{"_id": "legacy-item-$suffix", "name": "L", "url": "https://example.com/legacy",
             "_class": "fr.marstech.mtlinkspray.entity.DashboardLink"}], "slug": "legacy-$suffix", "theme": "DARK",
             "_class": "fr.marstech.mtlinkspray.entity.DashboardEntity"}""")
        insert("mt-link-spray-items", """
            {"_id": "Lg$suffix", "creationDate": {"${'$'}date": "2026-11-22T01:49:32.619Z"}, "isEnabled": true, "metadata": {},
             "author": {"dateTime": {"${'$'}date": "2026-11-22T01:49:32.619Z"}}, "historyItems": [],
             "target": {"targetUrl": "https://example.com/legacy-link"}, "clickCount": 7, "botClickCount": 2,
             "_class": "fr.marstech.mtlinkspray.entity.LinkItem"}""")
        insert("pastes", """
            {"_id": "legacy-paste-$suffix", "creationDate": {"${'$'}date": "2026-11-22T01:49:32.763Z"},
             "expiresAt": {"${'$'}date": "2126-11-22T01:49:32.763Z"}, "isEnabled": true, "metadata": {},
             "author": {"ipAddress": "127.0.0.1", "dateTime": {"${'$'}date": "2026-11-22T01:49:32.763Z"}, "action": "CREATE_PASTE"},
             "historyItems": [], "content": "legacy paste", "language": "TEXT", "isPrivate": false,
             "isPasswordProtected": false, "viewHistory": [], "_class": "fr.marstech.mtlinkspray.entity.PasteEntity"}""")
        insert("checklist_templates", """
            {"_id": "legacy-tpl-$suffix", "namespace": "$namespace", "title": "Legacy template",
             "items": [{"itemId": "legacy-a", "label": "a"}], "version": 0,
             "creationDate": {"${'$'}date": "2026-11-22T01:49:33.107Z"},
             "_class": "fr.marstech.mtlinkspray.entity.ChecklistTemplateEntity"}""")

        insert("checklist_instances", """
            {"_id": "legacy-inst-$suffix", "namespace": "$namespace", "templateId": "legacy-tpl-$suffix",
             "templateVersion": 0, "title": "Legacy run", "items": [{"itemId": "legacy-a", "label": "a", "status": "DONE"}],
             "creationDate": {"${'$'}date": "2026-11-22T01:49:33.200Z"},
             "_class": "fr.marstech.mtlinkspray.entity.ChecklistInstanceEntity"}""")
        insert("cron_jobs", """
            {"_id": "legacy-cron-$suffix", "namespace": "$namespace", "name": "Legacy job", "schedule": "0 0 3 * * *",
             "method": "GET", "url": "https://example.com/legacy-hook", "headers": {}, "timeoutSeconds": 10,
             "failureThreshold": 3, "isEnabled": true, "creationDate": {"${'$'}date": "2026-11-22T01:49:33.300Z"},
             "consecutiveFailures": 0, "_class": "fr.marstech.mtlinkspray.entity.CronJobEntity"}""")
        insert("images", """
            {"_id": "legacy-img-$suffix", "creationDate": {"${'$'}date": "2026-11-22T01:49:33.400Z"}, "isEnabled": true,
             "metadata": {}, "author": {"dateTime": {"${'$'}date": "2026-11-22T01:49:33.400Z"}}, "historyItems": [],
             "apiKeyId": "legacy-key-$suffix", "s3Key": "legacy/$suffix.png", "originalFilename": "legacy.png",
             "contentType": "image/png", "sizeBytes": 8, "isPrivate": false,
             "_class": "fr.marstech.mtlinkspray.entity.ImageEntity"}""")

        val dashboard = get("/api/dashboard/legacy-dash-$suffix")
        assertEquals(200, dashboard.statusCode(), dashboard.body())
        assertEquals("https://example.com/legacy", dashboard.json()["items"][0]["url"].asString())
        assertTrue(get("/p/legacy-$suffix").body().contains("Legacy board"))

        val stats = get("/api/url-shortener/Lg$suffix/stats").json()
        assertEquals(7, stats["clickCount"].asInt())
        assertEquals(2, stats["botClickCount"].asInt())
        assertEquals("https://example.com/legacy-link", get("/Lg$suffix").header("Location"))

        assertEquals("legacy paste", get("/api/paste/legacy-paste-$suffix/raw").body())

        val template = get("/api/checklists/templates/legacy-tpl-$suffix", mapOf(auth))
        assertEquals(200, template.statusCode(), template.body())
        assertEquals("legacy-a", template.json()["items"][0]["itemId"].asString())

        val instance = get("/api/checklists/instances/legacy-inst-$suffix", mapOf(auth))
        assertEquals(200, instance.statusCode(), instance.body())
        assertEquals("DONE", instance.json()["items"][0]["status"].asString())

        val job = get("/api/cron/jobs/legacy-cron-$suffix", mapOf(auth))
        assertEquals(200, job.statusCode(), job.body())
        assertEquals("https://example.com/legacy-hook", job.json()["url"].asString())

        // Owner check reads the legacy image and the legacy key's id
        assertEquals(204, delete("/api/images/legacy-img-$suffix", mapOf(auth)).statusCode())
    }

    @Test
    fun shouldKeepIndexShapes() {
        // The services create their indexes after ApplicationReadyEvent: wait for them, up to 15 s
        fun index(collection: String, name: String): Document {
            val deadline = System.currentTimeMillis() + 15_000
            while (true) {
                mongo.getCollection(collection).listIndexes().firstOrNull { it.getString("name") == name }?.let { return it }
                check(System.currentTimeMillis() < deadline) { "index $name missing on $collection" }
                Thread.sleep(200)
            }
        }

        assertEquals(2_592_000L, (index("cron_runs", "startedAt_1")["expireAfterSeconds"] as Number).toLong())
        index("cron_runs", "jobId_1_startedAt_-1")
        assertEquals(31_536_000L, (index("mt-link-spray-write-signatures", "createdAt_1")["expireAfterSeconds"] as Number).toLong())

        val slug = index("mt-link-spray-dashboard-entity", "slug_1")
        assertEquals(true, slug["unique"])
        assertEquals(true, slug["sparse"])
    }
}
