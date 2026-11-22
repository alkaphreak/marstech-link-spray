package fr.marstech.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

internal class CronE2eTest : E2eTest() {

    private val auth by lazy { mapOf(apiKey("cron-${UUID.randomUUID()}")) }

    // Yearly and disabled: no webhook fires while the suite runs
    private fun job(url: String = "https://example.com/hook", schedule: String = "0 0 0 1 1 *") =
        """{"name":"e2e job","schedule":"$schedule","method":"POST","url":"$url","isEnabled":false}"""

    @Test
    fun shouldRunJobLifecycle() {
        val created = postJson("/api/cron/jobs", job(), auth)
        assertEquals(201, created.statusCode(), created.body())
        val id = created.json()["id"].asString()

        assertEquals("e2e job", get("/api/cron/jobs/$id", auth).json()["name"].asString())
        assertEquals(1, get("/api/cron/jobs", auth).json().size())

        val runs = get("/api/cron/jobs/$id/runs?limit=10", auth)
        assertEquals(200, runs.statusCode())
        assertEquals(0, runs.json().size())

        val replaced = sendJson("PUT", "/api/cron/jobs/$id", job().replace("e2e job", "renamed"), auth)
        assertEquals(200, replaced.statusCode(), replaced.body())
        assertEquals("renamed", get("/api/cron/jobs/$id", auth).json()["name"].asString())

        assertEquals(204, delete("/api/cron/jobs/$id", auth).statusCode())
        assertEquals(404, get("/api/cron/jobs/$id", auth).statusCode())
    }

    @Test
    fun shouldRefusePrivateTargetsAndBadSchedules() {
        assertEquals(400, postJson("/api/cron/jobs", job(url = "http://127.0.0.1:8080/x"), auth).statusCode())
        assertEquals(400, postJson("/api/cron/jobs", job(url = "http://10.0.0.1/x"), auth).statusCode())
        assertEquals(400, postJson("/api/cron/jobs", job(schedule = "every minute"), auth).statusCode())
        assertEquals(400, postJson("/api/cron/jobs", job().replace("POST", "TRACE"), auth).statusCode())
    }

    @Test
    fun shouldRequireKeyAndIsolateNamespaces() {
        assertEquals(401, get("/api/cron/jobs").statusCode())
        val id = postJson("/api/cron/jobs", job(), auth).json()["id"].asString()
        val other = mapOf(apiKey("cron-other-${UUID.randomUUID()}"))
        assertEquals(404, get("/api/cron/jobs/$id", other).statusCode())
        assertEquals(404, get("/api/cron/jobs/unknown", auth).statusCode())
    }
}
