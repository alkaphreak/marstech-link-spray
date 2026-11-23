package fr.marstech.mtlinkspray.conf

import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import jakarta.servlet.FilterChain
import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.CONTENT_TOO_LARGE
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * Caps request bodies before Jackson reads them and before any X-Api-Key check or bean validation
 * (MLS-272). A Content-Length over the cap is answered 413 without reading the body; a declared
 * length within it is enforced by the container.
 *
 * - JSON bodies on MVC routes: a body with no Content-Length (chunked) is counted while it streams
 *   and stops past the cap with a [PayloadTooLargeException], answered 413 by GlobalRestExceptionHandler.
 *   Nothing is buffered, so the cap can stay high enough for pastes and large checklists.
 * - `/mcp`, whatever the Content-Type: the functional transport answers 500 to a failed read, so a
 *   chunked body is read here first, at most one byte past its small dedicated cap, answered 413
 *   before dispatch when it overflows, and replayed otherwise.
 */
@Component
class JsonBodySizeLimitFilter(
    @Value("\${mt.link-spray.json.max-body-bytes:8388608}") private val maxBodyBytes: Long,
    @Value("\${mt.link-spray.json.mcp-max-body-bytes:65536}") private val mcpMaxBodyBytes: Int,
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val length = request.contentLengthLong
        when {
            request.requestURI == MCP_PATH -> when {
                length > mcpMaxBodyBytes -> tooLarge(response, mcpMaxBodyBytes.toLong())
                length >= 0 -> chain.doFilter(request, response)
                else -> {
                    val body = request.inputStream.readNBytes(mcpMaxBodyBytes + 1)
                    if (body.size > mcpMaxBodyBytes) tooLarge(response, mcpMaxBodyBytes.toLong())
                    else chain.doFilter(BodyRequest(request, ByteArrayInputStream(body), body.size.toLong()), response)
                }
            }
            !isJson(request) -> chain.doFilter(request, response)
            length > maxBodyBytes -> tooLarge(response, maxBodyBytes)
            length >= 0 -> chain.doFilter(request, response)
            else -> chain.doFilter(BodyRequest(request, CountingInputStream(request.inputStream, maxBodyBytes), -1), response)
        }
    }

    private fun tooLarge(response: HttpServletResponse, limit: Long) =
        response.sendError(CONTENT_TOO_LARGE.value(), message(limit))

    private fun isJson(request: HttpServletRequest): Boolean {
        val type = request.contentType?.let { runCatching { MediaType.parseMediaType(it) }.getOrNull() } ?: return false
        return type.subtype == "json" || type.subtypeSuffix == "json"
    }

    /** Serves [body] as the request body; [length] is -1 when unknown (still streaming). */
    private class BodyRequest(request: HttpServletRequest, body: InputStream, private val length: Long) :
        HttpServletRequestWrapper(request) {
        private val stream = BlockingInputStream(body)

        override fun getInputStream(): ServletInputStream = stream

        override fun getReader(): BufferedReader {
            val charset = runCatching { Charset.forName(characterEncoding) }.getOrDefault(Charsets.UTF_8)
            return BufferedReader(InputStreamReader(stream, charset))
        }

        override fun getContentLength() = length.toInt()
        override fun getContentLengthLong() = length
    }

    /** Blocking reads only: neither MVC nor the stateless MCP transport reads request bodies asynchronously. */
    private class BlockingInputStream(private val delegate: InputStream) : ServletInputStream() {
        private var finished = false

        override fun read(): Int = delegate.read().also { if (it < 0) finished = true }
        override fun read(b: ByteArray, off: Int, len: Int): Int = delegate.read(b, off, len).also { if (it < 0) finished = true }
        override fun isFinished() = finished
        override fun isReady() = true
        override fun setReadListener(listener: ReadListener) = throw UnsupportedOperationException("Blocking reads only")
    }

    private class CountingInputStream(private val delegate: InputStream, private val limit: Long) : InputStream() {
        private var count = 0L

        override fun read(): Int = delegate.read().also { if (it >= 0) count(1) }
        override fun read(b: ByteArray, off: Int, len: Int): Int = delegate.read(b, off, len).also { if (it > 0) count(it.toLong()) }

        private fun count(n: Long) {
            count += n
            if (count > limit) throw PayloadTooLargeException(message(limit))
        }
    }

    companion object {
        private const val MCP_PATH = "/mcp"
        private fun message(limit: Long) = "Request body exceeds the maximum allowed size of $limit bytes"
    }
}
