package fr.marstech.mtlinkspray.adapter.outbound.urlhaus

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.net.URLDecoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.time.Clock
import java.time.Duration
import java.util.concurrent.Flow
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UrlhausReputationTest {

    private val http = mock<HttpClient>()
    private var now = 0L
    private val clock = mock<Clock> { on { millis() } doAnswer { now } }
    private val reputation = UrlhausReputation("key", http, clock)

    private fun response(status: Int, body: String) = mock<HttpResponse<String>> {
        on { statusCode() } doReturn status
        on { body() } doReturn body
    }

    private fun answer(status: Int, body: String) {
        val response = response(status, body)
        whenever(http.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())).thenReturn(response)
    }

    private fun verifyCalls(n: Int) = verify(http, times(n)).send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())

    @Test
    fun listedUrlIsMaliciousAndCached() {
        answer(200, """{"query_status":"ok","url_status":"online","threat":"malware_download"}""")

        assertTrue(reputation.isMalicious("https://evil.example/x"))
        assertTrue(reputation.isMalicious("https://evil.example/x"))
        verify(http, times(1)).send(
            argThat<HttpRequest> { headers().firstValue("Auth-Key").orElse(null) == "key" && method() == "POST" },
            any<HttpResponse.BodyHandler<String>>(),
        )
    }

    @Test
    fun unknownUrlIsSafe() {
        answer(200, NOT_LISTED)
        assertFalse(reputation.isMalicious("https://example.com"))
    }

    @Test
    fun aFailurePausesCallsForAMinuteAndIsNotCached() {
        answer(401, """{"query_status":"unknown_auth_key"}""")
        assertFalse(reputation.isMalicious("https://a.example"))
        assertFalse(reputation.isMalicious("https://b.example"))
        verifyCalls(1)

        now += 61_000
        answer(200, "not json")
        assertFalse(reputation.isMalicious("https://a.example"))
        verifyCalls(2)
    }

    @Test
    fun anInvalidUrlAnswerIsNotListedAndOpensNoPause() {
        answer(200, """{"query_status":"invalid_url"}""")
        assertFalse(reputation.isMalicious("https://a.example"))
        answer(200, LISTED)
        assertTrue(reputation.isMalicious(EVIL))
        verifyCalls(2)
    }

    @Test
    fun unreachableUrlhausLetsTheLinkThrough() {
        whenever(http.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())).doThrow(IOException("timeout"))
        assertFalse(reputation.isMalicious("https://example.com"))
    }

    @Test
    fun interruptedCallLetsTheLinkThroughAndKeepsTheFlag() {
        whenever(http.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())).doThrow(InterruptedException())
        assertFalse(reputation.isMalicious("https://example.com"))
        assertTrue(Thread.interrupted())
    }

    @Test
    fun anyMaliciousChecksEveryUrlOnceAndFindsTheListedOne() {
        whenever(http.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())).doAnswer {
            val listed = formUrl(it.getArgument(0)) == EVIL
            response(200, if (listed) LISTED else NOT_LISTED)
        }
        val urls = (1..20).map { "https://ok$it.example" } + EVIL + "https://ok1.example"

        assertTrue(reputation.anyMalicious(urls))
        verifyCalls(21)
        // Second pass comes from the cache
        assertTrue(reputation.anyMalicious(urls))
        assertFalse(reputation.anyMalicious(listOf("https://ok1.example", "https://ok2.example")))
        verifyCalls(21)
    }

    @Test
    fun aSprayPastItsDeadlineLetsTheSlowUrlsThroughWithoutAPause() {
        val slow = UrlhausReputation("key", http, clock, Duration.ofMillis(200))
        whenever(http.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>())).doAnswer {
            Thread.sleep(10_000)
            response(200, LISTED)
        }
        assertFalse(slow.anyMalicious(listOf("https://a.example", "https://b.example")))

        answer(200, LISTED)
        assertTrue(slow.isMalicious(EVIL))
    }

    @Test
    fun anyMaliciousWithOneUnknownUrlUsesASingleCall() {
        answer(200, NOT_LISTED)
        assertFalse(reputation.anyMalicious(listOf("https://example.com")))
        assertFalse(reputation.anyMalicious(emptyList()))
        verifyCalls(1)
    }

    private fun formUrl(request: HttpRequest): String {
        val sb = StringBuilder()
        request.bodyPublisher().get().subscribe(object : Flow.Subscriber<ByteBuffer> {
            override fun onSubscribe(s: Flow.Subscription) = s.request(Long.MAX_VALUE)
            override fun onNext(item: ByteBuffer) { sb.append(Charsets.UTF_8.decode(item)) }
            override fun onError(t: Throwable) = Unit
            override fun onComplete() = Unit
        })
        return URLDecoder.decode(sb.removePrefix("url=").toString(), Charsets.UTF_8)
    }

    private companion object {
        const val EVIL = "https://evil.example"
        const val LISTED = """{"query_status":"ok"}"""
        const val NOT_LISTED = """{"query_status":"no_results"}"""
    }
}
