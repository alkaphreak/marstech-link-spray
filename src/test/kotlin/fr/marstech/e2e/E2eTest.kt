package fr.marstech.e2e

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import org.jsoup.Jsoup
import java.net.CookieManager
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64

/**
 * Black-box base for the regression suite of MLS-277 (ADR 0007 prerequisite).
 * Only [MtLinkSprayApplication] may be imported from production code (ArchitectureE2eTest),
 * so the hexagonal migration can neither move nor rewrite these tests.
 */
@SpringBootTest(classes = [MtLinkSprayApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Import(E2eMailConfiguration::class)
abstract class E2eTest {

    @Value("\${local.server.port}")
    protected var port: Int = 0

    @Autowired
    protected lateinit var mongo: MongoTemplate

    // No redirect following: 302s are asserted as such
    private val http: HttpClient = HttpClient.newHttpClient()
    protected val json: JsonMapper = JsonMapper.builder().build()

    protected fun url(path: String) = URI("http://localhost:$port$path")

    protected fun send(
        method: String,
        path: String,
        body: HttpRequest.BodyPublisher = HttpRequest.BodyPublishers.noBody(),
        headers: Map<String, String> = emptyMap(),
    ): HttpResponse<String> = stringView(sendBytes(method, path, body, headers))

    protected fun sendBytes(
        method: String,
        path: String,
        body: HttpRequest.BodyPublisher = HttpRequest.BodyPublishers.noBody(),
        headers: Map<String, String> = emptyMap(),
    ): HttpResponse<ByteArray> {
        val builder = HttpRequest.newBuilder(url(path)).method(method, body)
        headers.forEach { (k, v) -> builder.header(k, v) }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray())
    }

    protected fun get(path: String, headers: Map<String, String> = emptyMap()) = send("GET", path, headers = headers)

    protected fun delete(path: String, headers: Map<String, String> = emptyMap()) =
        send("DELETE", path, headers = headers)

    protected fun postJson(path: String, body: String, headers: Map<String, String> = emptyMap()) =
        sendJson("POST", path, body, headers)

    protected fun sendJson(method: String, path: String, body: String, headers: Map<String, String> = emptyMap()) =
        send(method, path, HttpRequest.BodyPublishers.ofString(body), headers + ("Content-Type" to "application/json"))

    protected fun postForm(path: String, fields: List<Pair<String, String>>, headers: Map<String, String> = emptyMap()) =
        send(
            "POST", path,
            HttpRequest.BodyPublishers.ofString(form(fields)),
            headers + ("Content-Type" to "application/x-www-form-urlencoded"),
        )

    protected fun postRaw(path: String, bytes: ByteArray, contentType: String, headers: Map<String, String> = emptyMap()) =
        send("POST", path, HttpRequest.BodyPublishers.ofByteArray(bytes), headers + ("Content-Type" to contentType))

    /** Admin browser: HTTP Basic, its own session cookie, CSRF token read from the page like a real form. */
    protected inner class Admin(user: String = ADMIN_USER, password: String = ADMIN_PASSWORD) {
        private val client = HttpClient.newBuilder().cookieHandler(CookieManager()).build()
        private val auth = basic(user, password)

        fun get(path: String): HttpResponse<String> = client.send(
            HttpRequest.newBuilder(url(path)).header(auth.first, auth.second).GET().build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        fun csrf(path: String = "/admin/api-keys"): String =
            Jsoup.parse(get(path).body()).selectFirst("input[name=_csrf]")!!.attr("value")

        fun post(path: String, fields: List<Pair<String, String>>, withCsrf: Boolean = true): HttpResponse<String> {
            val all = if (withCsrf) fields + ("_csrf" to csrf()) else fields
            return client.send(
                HttpRequest.newBuilder(url(path)).header(auth.first, auth.second)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form(all))).build(),
                HttpResponse.BodyHandlers.ofString(),
            )
        }

        /** Creates a key through the admin page and returns the raw `{keyId}.{secret}` shown once. */
        fun createApiKey(namespace: String, name: String = "e2e-$namespace"): String {
            val created = post("/admin/api-keys", listOf("name" to name, "namespace" to namespace))
            check(created.statusCode() == 302) { "key creation answered ${created.statusCode()}" }
            return Jsoup.parse(get("/admin/api-keys").body()).selectFirst("pre")!!.text()
        }
    }

    /** Path and query of an absolute URL the app returned, to call it on this server. */
    protected fun pathOf(absoluteUrl: String): String =
        URI(absoluteUrl.trim()).let { it.rawPath + (it.rawQuery?.let { q -> "?$q" } ?: "") }

    /** Follows a short URL the app returned: asserts the 302 and gives its target. */
    protected fun redirectOf(shortUrl: String): String {
        val response = get(pathOf(shortUrl), mapOf("User-Agent" to "Mozilla/5.0 (Macintosh) Firefox/140.0"))
        check(response.statusCode() == 302) { "$shortUrl answered ${response.statusCode()}" }
        return response.header("Location")!!
    }

    protected fun apiKey(namespace: String) = "X-Api-Key" to Admin().createApiKey(namespace)

    protected fun HttpResponse<String>.json(): JsonNode = json.readTree(body())

    protected fun HttpResponse<String>.header(name: String): String? = headers().firstValue(name).orElse(null)

    protected fun basic(user: String, password: String) =
        "Authorization" to "Basic " + Base64.getEncoder().encodeToString("$user:$password".toByteArray())

    protected fun enc(value: String): String = URLEncoder.encode(value, Charsets.UTF_8)

    protected fun decode(value: String): String = URLDecoder.decode(value, Charsets.UTF_8)

    companion object {
        // application-e2e.properties
        const val ADMIN_USER = "admin"
        const val ADMIN_PASSWORD = "test-admin-password"

        @JvmStatic
        @DynamicPropertySource
        fun objectStorage(registry: DynamicPropertyRegistry) {
            registry.add("mt.link-spray.storage.do-spaces.endpoint") { FakeS3.endpoint }
        }
    }

    private fun form(fields: List<Pair<String, String>>) = fields.joinToString("&") { (k, v) -> "${enc(k)}=${enc(v)}" }

    private fun stringView(r: HttpResponse<ByteArray>): HttpResponse<String> = object : HttpResponse<String> {
        override fun statusCode() = r.statusCode()
        override fun request() = r.request()
        override fun previousResponse() = java.util.Optional.empty<HttpResponse<String>>()
        override fun headers() = r.headers()
        override fun body() = String(r.body(), Charsets.UTF_8)
        override fun sslSession() = r.sslSession()
        override fun uri() = r.uri()
        override fun version() = r.version()
    }
}
