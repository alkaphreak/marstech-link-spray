package fr.marstech.mtlinkspray.controller.commons

import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.TypeMismatchException
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

// Top-level dummy used by several tests
@Suppress("unused", "UNUSED_PARAMETER")
class Dummy {
    fun method(param: String) {}
}

class GlobalRestExceptionHandlerTest {

    private val handler = GlobalRestExceptionHandler()
    private val mockRequest: WebRequest = mock(WebRequest::class.java).apply {
        `when`(getDescription(false)).thenReturn("uri=/api/test")
    }

    @Test
    fun shouldReturn500ForRuntimeException() {
        val ex = RuntimeException("runtime error")
        val response = handler.handleRuntimeException(ex, mockRequest)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)
        assertEquals(500, response.body?.status)
        assertEquals("runtime error", response.body?.message)
    }

    @Test
    fun shouldReturn500ForGeneralException() {
        val ex = Exception("general error")
        val response = handler.handleException(ex, mockRequest)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)
        assertEquals(500, response.body?.status)
        assertEquals("general error", response.body?.message)
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

    @Suppress("DEPRECATION")
    @Test
    fun shouldReturn400ForJsonParseException() {
        val ex = HttpMessageNotReadableException("JSON parse error", RuntimeException("cause"))
        val response = handler.handleJsonParseException(ex, mockRequest)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body?.status)
        val message = response.body?.message
        assertTrue(message?.contains("JSON") ?: false)
    }
}