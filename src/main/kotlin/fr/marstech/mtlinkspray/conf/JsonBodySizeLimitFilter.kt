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
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * Caps the size of every JSON request body (MLS-272), before Jackson reads it and before any
 * X-Api-Key check or bean validation, on every route (MVC controllers and the /mcp endpoint).
 * A Content-Length over the limit is answered 413 here, without reading the body. A body with
 * no Content-Length (chunked) stops being read past the limit with a [PayloadTooLargeException],
 * answered 413 by GlobalRestExceptionHandler. Binary bodies (images, PDF, OCR) keep their own limits.
 */
@Component
class JsonBodySizeLimitFilter(
    @Value("\${mt.link-spray.json.max-body-bytes:8388608}") private val maxBodyBytes: Long,
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        when {
            !isJson(request) -> chain.doFilter(request, response)
            request.contentLengthLong > maxBodyBytes -> response.sendError(CONTENT_TOO_LARGE.value(), message(maxBodyBytes))
            else -> chain.doFilter(LimitedRequest(request, maxBodyBytes), response)
        }
    }

    private fun isJson(request: HttpServletRequest): Boolean {
        val type = request.contentType?.let { runCatching { MediaType.parseMediaType(it) }.getOrNull() } ?: return false
        return type.subtype == "json" || type.subtypeSuffix == "json"
    }

    private class LimitedRequest(request: HttpServletRequest, private val limit: Long) : HttpServletRequestWrapper(request) {
        private val stream by lazy { LimitedInputStream(request.inputStream, limit) }

        override fun getInputStream(): ServletInputStream = stream

        override fun getReader(): BufferedReader {
            val charset = runCatching { Charset.forName(characterEncoding) }.getOrDefault(Charsets.UTF_8)
            return BufferedReader(InputStreamReader(stream, charset))
        }
    }

    private class LimitedInputStream(private val delegate: ServletInputStream, private val limit: Long) : ServletInputStream() {
        private var count = 0L

        override fun read(): Int = delegate.read().also { if (it >= 0) count(1) }

        override fun read(b: ByteArray, off: Int, len: Int): Int = delegate.read(b, off, len).also { if (it > 0) count(it.toLong()) }

        private fun count(n: Long) {
            count += n
            if (count > limit) throw PayloadTooLargeException(message(limit))
        }

        override fun isFinished() = delegate.isFinished
        override fun isReady() = delegate.isReady
        override fun setReadListener(listener: ReadListener) = delegate.setReadListener(listener)
    }

    companion object {
        private fun message(limit: Long) = "JSON request body exceeds the maximum allowed size of $limit bytes"
    }
}
