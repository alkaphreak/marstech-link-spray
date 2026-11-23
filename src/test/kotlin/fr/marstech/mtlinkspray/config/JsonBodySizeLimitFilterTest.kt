package fr.marstech.mtlinkspray.config
import fr.marstech.mtlinkspray.adapter.inbound.web.common.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.domain.shared.PayloadTooLargeException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletContext
import jakarta.servlet.ServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

private const val LIMIT = 64L
private const val MCP_LIMIT = 32
private const val ECHO = "/echo"
private const val ACCENTED_JSON = """{"k":"é"}"""

class JsonBodySizeLimitFilterTest {

    @RestController
    class EchoController {
        @PostMapping(ECHO)
        fun echo(@RequestBody body: Map<String, Any>) = body

        @PostMapping("/raw")
        fun raw(@RequestBody body: String) = body.length.toString()
    }

    /** A request with no Content-Length, as a chunked transfer arrives. */
    private class ChunkedRequest(context: ServletContext? = null) : MockHttpServletRequest(context, "POST", ECHO) {
        override fun getContentLengthLong() = -1L
        override fun getContentLength() = -1
    }

    private val filter = JsonBodySizeLimitFilter(LIMIT, MCP_LIMIT)
    private val mockMvc = MockMvcBuilders.standaloneSetup(EchoController())
        .setControllerAdvice(GlobalRestExceptionHandler())
        .addFilters<StandaloneMockMvcBuilder>(filter)
        .build()

    private fun json(size: Int) = """{"k":"${"a".repeat(size - 8)}"}"""

    private fun passedRequest(request: MockHttpServletRequest, response: MockHttpServletResponse = MockHttpServletResponse()): ServletRequest? {
        var passed: ServletRequest? = null
        filter.doFilter(request, response) { req, _ -> passed = req }
        return passed
    }

    @Test
    fun shouldAcceptJsonBodyAtTheLimit() {
        mockMvc.perform(post(ECHO).contentType(APPLICATION_JSON).content(json(LIMIT.toInt())))
            .andExpect(status().isOk)
    }

    @Test
    fun shouldRejectDeclaredLengthOverTheLimitWithoutReadingTheBody() {
        mockMvc.perform(post(ECHO).contentType(APPLICATION_JSON).content(json(LIMIT.toInt() + 1)))
            .andExpect(status().`is`(413))
    }

    @Test
    fun shouldApplyTheLimitToJsonSuffixTypes() {
        mockMvc.perform(post(ECHO).contentType("application/merge-patch+json").content(json(LIMIT.toInt() + 1)))
            .andExpect(status().`is`(413))
    }

    @Test
    fun shouldLeaveNonJsonBodiesUntouched() {
        mockMvc.perform(post("/raw").contentType(TEXT_PLAIN).content("a".repeat(LIMIT.toInt() * 2)))
            .andExpect(status().isOk)
            .andExpect(content().string("${LIMIT * 2}"))
    }

    @Test
    fun shouldPassRequestsWithoutOrWithInvalidContentTypeUnwrapped() {
        val noType = MockHttpServletRequest("POST", ECHO)
        val badType = MockHttpServletRequest("POST", ECHO).apply { contentType = "not a media type" }

        assertSame(noType, passedRequest(noType))
        assertSame(badType, passedRequest(badType))
    }

    @Test
    fun shouldAnswerContentTooLargeWhenAChunkedJsonBodyOverflowsDuringParsing() {
        // Given a chunked JSON body over the limit, parsed by Jackson behind the filter
        val chunked = RequestPostProcessor { built ->
            ChunkedRequest(built.servletContext).apply {
                contentType = APPLICATION_JSON_VALUE
                setContent(json(LIMIT.toInt() * 4).toByteArray())
            }
        }

        // When / Then
        mockMvc.perform(post(ECHO).with(chunked))
            .andExpect(status().`is`(413))
    }

    @Test
    fun shouldStopReadingAChunkedJsonBodyPastTheLimit() {
        val request = ChunkedRequest().apply {
            contentType = APPLICATION_JSON_VALUE
            setContent(ByteArray(LIMIT.toInt() * 4) { 'a'.code.toByte() })
        }
        val chain = FilterChain { req, _ -> while (req.inputStream.read() >= 0) Unit }

        assertThrows<PayloadTooLargeException> { filter.doFilter(request, MockHttpServletResponse(), chain) }
    }

    @Test
    fun shouldReadThroughTheReaderWithTheRequestCharsetOrUtf8() {
        val latin1 = ChunkedRequest().apply {
            contentType = "$APPLICATION_JSON_VALUE;charset=ISO-8859-1"
            setContent(ACCENTED_JSON.toByteArray(Charsets.ISO_8859_1))
        }
        val bogus = ChunkedRequest().apply {
            contentType = APPLICATION_JSON_VALUE
            characterEncoding = "bogus-charset"
            setContent(ACCENTED_JSON.toByteArray())
        }

        assertEquals(ACCENTED_JSON, passedRequest(latin1)!!.reader.readText())
        assertEquals(ACCENTED_JSON, passedRequest(bogus)!!.reader.readText())
    }

    private fun mcp(body: ByteArray, type: String? = APPLICATION_JSON_VALUE) =
        ChunkedRequest().apply { requestURI = "/mcp"; contentType = type; setContent(body) }

    @Test
    fun shouldAnswerContentTooLargeOnMcpBeforeDispatchWhateverTheContentType() {
        // Given chunked /mcp bodies over the MCP cap, declared as JSON, as text or with no type
        for (type in listOf(APPLICATION_JSON_VALUE, "text/plain", null)) {
            val response = MockHttpServletResponse()

            // When / Then: the transport is never reached
            assertNull(passedRequest(mcp(ByteArray(MCP_LIMIT + 1), type), response))
            assertEquals(413, response.status)
        }
    }

    @Test
    fun shouldAnswerContentTooLargeOnMcpForADeclaredLengthOverItsCap() {
        val request = MockHttpServletRequest("POST", "/mcp").apply { contentType = "text/plain"; setContent(ByteArray(MCP_LIMIT + 1)) }
        val response = MockHttpServletResponse()

        assertNull(passedRequest(request, response))
        assertEquals(413, response.status)
    }

    @Test
    fun shouldReplayAChunkedMcpBodyWithinItsCap() {
        val body = ByteArray(MCP_LIMIT) { 'a'.code.toByte() }

        val passed = passedRequest(mcp(body))!!
        assertEquals(MCP_LIMIT.toLong(), passed.contentLengthLong)
        assertEquals(String(body), String(passed.inputStream.readAllBytes()))
        assertTrue(passed.inputStream.isFinished)
    }
}
