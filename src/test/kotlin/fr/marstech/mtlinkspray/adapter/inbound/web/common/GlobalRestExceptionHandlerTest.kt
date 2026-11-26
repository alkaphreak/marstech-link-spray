package fr.marstech.mtlinkspray.adapter.inbound.web.common
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.TypeMismatchException
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindException
import org.springframework.validation.method.MethodValidationException
import org.springframework.validation.method.MethodValidationResult
import org.springframework.validation.method.ParameterValidationResult
import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import org.springframework.http.HttpMethod

// Top-level dummy used by several tests
@Suppress("UNUSED_PARAMETER")
class Dummy {
    fun method(param: String) {
        // No implementation needed for testing purposes
    }
}

class GlobalRestExceptionHandlerTest {

    private val handler = GlobalRestExceptionHandler()
    private val mockRequest: WebRequest = mock(WebRequest::class.java).apply {
        `when`(getDescription(false)).thenReturn("uri=/api/test")
    }

    @Test
    fun shouldReportConfiguredUploadLimitIn413() {
        val handler = GlobalRestExceptionHandler(org.springframework.util.unit.DataSize.ofMegabytes(10))
        val ex = org.springframework.web.multipart.MaxUploadSizeExceededException(-1)
        val response = handler.handleMaxUploadSizeExceeded(ex, mockRequest)
        assertEquals(HttpStatus.CONTENT_TOO_LARGE, response.statusCode)
        assertEquals("File exceeds the maximum upload size of 10 MB", response.body?.message)
    }

    @Test
    fun shouldReportExactBytesForNonWholeMegabyteLimit() {
        val handler = GlobalRestExceptionHandler(org.springframework.util.unit.DataSize.ofKilobytes(512))
        val ex = org.springframework.web.multipart.MaxUploadSizeExceededException(-1)
        val response = handler.handleMaxUploadSizeExceeded(ex, mockRequest)
        assertEquals("File exceeds the maximum upload size of 524288 bytes", response.body?.message)
    }

    @Test
    fun shouldReturn500ForRuntimeException() {
        val ex = RuntimeException("runtime error")
        val response = handler.handleRuntimeException(ex, mockRequest)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)
        assertEquals(500, response.body?.status)
        assertEquals("Internal server error", response.body?.message)
    }

    @Test
    fun shouldReturn500ForGeneralException() {
        val ex = Exception("general error")
        val response = handler.handleException(ex, mockRequest)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)
        assertEquals(500, response.body?.status)
        assertEquals("Internal server error", response.body?.message)
    }

    @Test
    fun shouldKeepAllowHeaderOn405() {
        val ex = org.springframework.web.HttpRequestMethodNotSupportedException("GET", listOf("POST"))
        val response = handler.handleException(ex, mockRequest)
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.statusCode)
        assertEquals(setOf(HttpMethod.POST), response.headers.allow)
    }

    @Test
    fun shouldReturn400ForIllegalArgument() {
        val ex = IllegalArgumentException("bad argument")
        val response = handler.handleIllegalArgument(ex, mockRequest)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body?.status)
        assertEquals("bad argument", response.body?.message)
    }

    @Test
    fun shouldReturn404ForNotFound() {
        val ex = NoSuchElementException("not found")
        val response = handler.handleNotFound(ex, mockRequest)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(response.body)
        assertEquals(404, response.body?.status)
        assertEquals("not found", response.body?.message)
    }

    @Test
    fun shouldReturn404ForNoResourceFound() {
        val ex = NoResourceFoundException(HttpMethod.GET, "/robots.txt", "robots.txt")
        val response = handler.handleNoResourceFound(ex, mockRequest)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(response.body)
        assertEquals(404, response.body?.status)
        assertEquals("The requested resource does not exist", response.body?.message)
    }

    @Test
    fun shouldReturn401ForUnauthorized() {
        val ex = IllegalAccessException("unauthorized")
        val response = handler.handleUnauthorized(ex, mockRequest)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(401, response.body?.status)
        assertEquals("unauthorized", response.body?.message)
    }

    @Test
    fun shouldReturn400ForMethodArgumentTypeMismatch() {
        val cause = NumberFormatException("For input string: \"1000000000000\"")
        val method = Dummy::class.java.getDeclaredMethod("method", String::class.java)
        val mp = MethodParameter(method, 0)
        val ex = MethodArgumentTypeMismatchException("1000000000000", Int::class.java, "max", mp, cause)
        val response = handler.handleMethodArgumentTypeMismatch(ex, mockRequest)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body?.status)
        val message = response.body?.message
        assertTrue(message?.contains("For input string") ?: false)
    }

    @Test
    fun shouldReturn400ForTypeMismatch() {
        val ex = TypeMismatchException("1000000000000", Int::class.java)
        val response = handler.handleTypeMismatch(ex, mockRequest)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body?.status)
        val message = response.body?.message
        assertTrue(message?.isNotBlank() ?: false)
    }

    @Test
    fun shouldMapDomainErrorsToTheirStatus() {
        val cases = listOf(
            handler.handleForbidden(fr.marstech.mtlinkspray.domain.shared.ForbiddenException("not yours"), mockRequest) to HttpStatus.FORBIDDEN,
            handler.handleDomainConflict(fr.marstech.mtlinkspray.domain.shared.ConflictException("taken"), mockRequest) to HttpStatus.CONFLICT,
            handler.handleBusy(fr.marstech.mtlinkspray.domain.shared.BusyException("busy"), mockRequest) to HttpStatus.SERVICE_UNAVAILABLE,
        )
        cases.forEach { (response, status) ->
            assertEquals(status, response.statusCode)
            assertEquals(status.reasonPhrase, response.body?.error)
            assertEquals("/api/test", response.body?.path)
        }
        assertEquals(listOf("not yours", "taken", "busy"), cases.map { it.first.body?.message })
    }

    @Test
    fun shouldKeepAnAdapterResponseStatus() {
        val e = org.springframework.web.server.ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Image storage not configured")
        val response = handler.handleResponseStatus(e, mockRequest)
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.statusCode)
        assertEquals("Service Unavailable", response.body?.error)
        assertEquals("Image storage not configured", response.body?.message)
    }

    @Test
    fun shouldAnswerQuotaExceededWithRetryAfter() {
        val response = handler.handleQuotaExceeded(fr.marstech.mtlinkspray.domain.shared.QuotaExceededException("quota reached", 42), mockRequest)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.statusCode)
        assertEquals("quota reached", response.body?.message)
        assertEquals("42", response.headers.getFirst(org.springframework.http.HttpHeaders.RETRY_AFTER))
    }

    @Test
    fun shouldReturn400ForMissingParameter() {
        val ex = MissingServletRequestParameterException("max", "int")
        val response = handler.handleMissingParameter(ex, mockRequest)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body?.status)
        val message = response.body?.message
        assertTrue(message?.isNotBlank() ?: false)
    }

    @Test
    fun shouldReturn400ForValidationException() {
        val ex = ConstraintViolationException("violations", emptySet())
        val response = handler.handleValidationException(ex, mockRequest)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body?.status)
        assertEquals("Validation Failed", response.body?.error)
    }

    @Test
    fun shouldReturn400ForMethodArgumentNotValid() {
        val method = Dummy::class.java.getDeclaredMethod("method", String::class.java)
        val mp = MethodParameter(method, 0)
        val binding = BeanPropertyBindingResult(Dummy(), "dummy")
        val ex = MethodArgumentNotValidException(mp, binding)
        val response = handler.handleMethodArgumentNotValid(ex, mockRequest)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body?.status)
        assertEquals("Validation Failed", response.body?.error)
    }

    @Test
    fun shouldReturn400ForBindException() {
        val ex = BindException(Dummy(), "obj")
        val response = handler.handleBindException(ex, mockRequest)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body?.status)
        assertEquals("Binding Failed", response.body?.error)
    }

    @Test
    fun shouldReturn400ForJsonParseException() {
        val ex = HttpMessageNotReadableException(
            "JSON parse error",
            RuntimeException("cause"),
            mock(HttpInputMessage::class.java)
        )
        val response = handler.handleJsonParseException(ex, mockRequest)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body?.status)
        val message = response.body?.message
        assertTrue(message?.contains("JSON") ?: false)
    }

    @Test
    fun shouldReturn400WithFieldErrorsForMethodValidationException() {
        // No @RequestParam: the field falls back to the method parameter name
        val param = MethodParameter(Dummy::class.java.getMethod("method", String::class.java), 0)
        val error = DefaultMessageSourceResolvable(null, null, "must not be blank")
        val paramResult = ParameterValidationResult(param, "", listOf(error), null, null, null) { e, _ -> e }
        val validationResult = mock(MethodValidationResult::class.java)
        `when`(validationResult.parameterValidationResults).thenReturn(listOf(paramResult))

        val response = handler.handleMethodValidation(MethodValidationException(validationResult), mockRequest)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("param", response.body?.fieldErrors?.single()?.field)
        assertEquals("must not be blank", response.body?.fieldErrors?.single()?.message)
    }

    @Test
    fun shouldRenderTheErrorPageWith403ForABrowser() {
        val page = handler.handleForbiddenPage(fr.marstech.mtlinkspray.domain.shared.ForbiddenException("malicious"))
        assertEquals(HttpStatus.FORBIDDEN, page.status)
        assertEquals("malicious", page.model["errorMessage"])
    }
}
