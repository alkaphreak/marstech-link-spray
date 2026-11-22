package fr.marstech.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

internal class DevToolsE2eTest : E2eTest() {

    @Test
    fun shouldServeVersionAndUuid() {
        assertEquals(200, get("/api/version").statusCode())
        UUID.fromString(get("/api/uuid").body().trim())
    }

    @Test
    fun shouldDrawRandomNumberInRange() {
        val value = get("/api/random?min=5&max=6").json()["value"].asInt()
        assertTrue(value in 5..6)
        assertEquals(400, get("/api/random").statusCode())
        assertEquals(400, get("/api/random?min=9&max=1").statusCode())
        assertEquals(400, get("/api/random?max=abc").statusCode())
    }

    @Test
    fun shouldComputeSleepCycles() {
        val response = get("/api/sleep?wakeAt=07:00")
        assertEquals(200, response.statusCode(), response.body())
        assertTrue(response.json()["options"].size() > 0)
        assertEquals(400, get("/api/sleep").statusCode())
        assertEquals(400, get("/api/sleep?wakeAt=07:00&sleepAt=23:00").statusCode())
        assertEquals(400, get("/api/sleep?wakeAt=7h").statusCode())
    }

    @Test
    fun shouldRenderQrCodes() {
        val png = sendBytes("GET", "/api/qr?text=${enc("https://sol4.space")}&size=128")
        assertEquals(200, png.statusCode())
        assertEquals("image/png", png.headers().firstValue("Content-Type").orElse(null))

        val svg = get("/api/qr?text=hello&format=svg")
        assertTrue(svg.body().contains("<svg"))

        val zip = sendBytes("POST", "/api/qr/bulk?text=${enc("one\ntwo")}")
        assertEquals(200, zip.statusCode())
        assertEquals("application/zip", zip.headers().firstValue("Content-Type").orElse(null))

        assertEquals(400, get("/api/qr?text=hello&size=10").statusCode())
        assertEquals(400, get("/api/qr?text=hello&fg=red").statusCode())
        assertEquals(400, send("POST", "/api/qr/bulk?text=").statusCode())
    }

    @Test
    fun shouldLookUpIpInTestDatabase() {
        val response = get("/api/ip?ip=8.8.8.8")
        assertEquals(200, response.statusCode(), response.body())
        assertEquals("8.8.8.8", response.json()["ip"].asString())
        assertEquals(400, get("/api/ip?ip=999.1.1.1").statusCode())
    }

    /** Needs outbound DNS to public resolvers. */
    @Test
    fun shouldResolveDns() {
        val response = get("/api/dns?name=example.com&type=A")
        assertEquals(200, response.statusCode(), response.body())
        assertEquals("example.com", response.json()["name"].asString().trimEnd('.'))
        assertTrue(response.json()["records"].size() > 0, response.body())
        assertEquals(400, get("/api/dns?name=example.com&type=BOGUS").statusCode())
        val reverse = get("/api/dns/reverse?ip=1.1.1.1")
        assertEquals(200, reverse.statusCode())
        assertTrue(reverse.json()["records"].size() > 0, reverse.body())
        assertEquals(400, get("/api/dns/reverse?ip=nope").statusCode())

        val propagation = get("/api/dns/propagation?name=example.com&type=A")
        assertEquals(200, propagation.statusCode(), propagation.body())
        assertEquals(4, propagation.json()["results"].size())
        assertTrue(propagation.json()["complete"].asBoolean(), propagation.body())
        for (i in 0 until 4) assertTrue(propagation.json()["results"][i]["records"].size() > 0, propagation.body())
        assertEquals(400, get("/api/dns/propagation?name=example.com&type=BOGUS").statusCode())
    }

    @Test
    fun shouldBuildSprays() {
        val plain = get("/api/spray?inputLinkList=${enc("https://a.example")}&inputLinkList=${enc("https://b.example")}")
        assertEquals(200, plain.statusCode(), plain.body())
        val sprayPage = get(pathOf(plain.body()))
        assertEquals(200, sprayPage.statusCode(), plain.body())
        assertTrue(sprayPage.body().contains("https://a.example") && sprayPage.body().contains("https://b.example"))

        val shortened = get("/api/spray?shorten=true&inputLinkList=${enc("https://c.example")}")
        assertEquals(200, shortened.statusCode(), shortened.body())
        // The spray link is shortened, and so is each URL in it
        val sprayTarget = redirectOf(shortened.body())
        assertTrue(sprayTarget.contains("/spray/open?spray="), sprayTarget)
        assertEquals("https://c.example", redirectOf(sprayTarget.substringAfter("spray=").let(::decode)))

        assertEquals(400, get("/api/spray?inputLinkList=${enc("not a url")}").statusCode())
        val tooMany = (1..101).joinToString("&") { "inputLinkList=${enc("https://x$it.example")}" }
        assertEquals(400, get("/api/spray?shorten=true&$tooMany").statusCode())
    }

    @Test
    fun shouldReportHealth() {
        assertEquals(200, get("/actuator/health").statusCode())
    }
}
