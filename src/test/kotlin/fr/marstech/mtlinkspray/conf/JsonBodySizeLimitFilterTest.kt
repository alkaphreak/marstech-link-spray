package fr.marstech.mtlinkspray.conf

import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletContext
import jakarta.servlet.ServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
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

    private val filter = JsonBodySizeLimitFilter(LIMIT)
    private val mockMvc = MockMvcBuilders.standaloneSetup(EchoController())
        .setControllerAdvice(GlobalRestExceptionHandler())
        .addFilters<StandaloneMockMvcBuilder>(filter)
        .build()

    private fun json(size: Int) = """{"k":"${"a".repeat(size - 8)}"}"""

    private fun passedRequest(request: MockHttpServletRequest): ServletRequest? {
        var passed: ServletRequest? = null
        filter.doFilter(request, MockHttpServletResponse()) { req, _ -> passed = req }
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
}
