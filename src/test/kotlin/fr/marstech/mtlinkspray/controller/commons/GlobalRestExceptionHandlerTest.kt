package fr.marstech.mtlinkspray.controller.commons

import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.TypeMismatchException
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

// Top-level dummy used by several tests
@Suppress("unused", "UNUSED_PARAMETER")
class Dummy {
    fun method(param: String) {}
}

class GlobalRestExceptionHandlerTest {

    private val handler = GlobalRestExceptionHandler()

    @Test
    fun shouldReturn500ForRuntimeException() {
        val ex = RuntimeException("runtime error")
        val response = handler.handleRuntimeException(ex)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("runtime error", response.body?.get("error"))
    }

    @Test
    fun shouldReturn500ForGeneralException() {
        val ex = Exception("general error")
        val response = handler.handleException(ex)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("general error", response.body?.get("error"))
    }

    @Test
    fun shouldReturn400ForIllegalArgument() {
        val ex = IllegalArgumentException("bad argument")
        val response = handler.handleIllegalArgument(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("bad argument", response.body?.get("error"))
    }

    @Test
    fun shouldReturn404ForNotFound() {
        val ex = NoSuchElementException("not found")
        val response = handler.handleNotFound(ex)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("not found", response.body?.get("error"))
    }

    @Test
    fun shouldReturn401ForUnauthorized() {
        val ex = IllegalAccessException("unauthorized")
        val response = handler.handleUnauthorized(ex)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("unauthorized", response.body?.get("error"))
    }

    @Test
    fun shouldReturn400ForMethodArgumentTypeMismatch() {
        val cause = NumberFormatException("For input string: \"1000000000000\"")
        val method = Dummy::class.java.getDeclaredMethod("method", String::class.java)
        val mp = MethodParameter(method, 0)
        val ex = MethodArgumentTypeMismatchException("1000000000000", Int::class.java, "max", mp, cause)
        val response = handler.handleMethodArgumentTypeMismatch(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        // message can include details; ensure it contains the cause message
        val error = response.body?.get("error")
        assertTrue(error?.contains("For input string") ?: false)
    }

    @Test
    fun shouldReturn400ForTypeMismatch() {
        val ex = TypeMismatchException("1000000000000", Int::class.java)
        val response = handler.handleTypeMismatch(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val error = response.body?.get("error")
        assertTrue(error?.isNotBlank() ?: false)
    }

    @Test
    fun shouldReturn400ForMissingParameter() {
        val ex = MissingServletRequestParameterException("max", "int")
        val response = handler.handleMissingParameter(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val error = response.body?.get("error")
        assertTrue(error?.isNotBlank() ?: false)
    }

    @Test
    fun shouldReturn400ForValidationException() {
        val ex = ConstraintViolationException("violations", emptySet())
        val response = handler.handleValidationException(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val error = response.body?.get("error")
        assertTrue(error?.isNotBlank() ?: false)
    }

    @Test
    fun shouldReturn400ForMethodArgumentNotValid() {
        val method = Dummy::class.java.getDeclaredMethod("method", String::class.java)
        val mp = MethodParameter(method, 0)
        val binding = BeanPropertyBindingResult(Dummy(), "dummy")
        val ex = MethodArgumentNotValidException(mp, binding)
        val response = handler.handleMethodArgumentNotValid(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val error = response.body?.get("error")
        assertTrue(error?.isNotBlank() ?: false)
    }

    @Test
    fun shouldReturn400ForBindException() {
        val ex = BindException(Dummy(), "obj")
        val response = handler.handleBindException(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val error = response.body?.get("error")
        assertTrue(error?.isNotBlank() ?: false)
    }

    @Suppress("DEPRECATION")
    @Test
    fun shouldReturn400ForJsonParseException() {
        val ex = HttpMessageNotReadableException("JSON parse error", RuntimeException("cause"))
        val response = handler.handleJsonParseException(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val error = response.body?.get("error")
        assertTrue(error?.contains("JSON") ?: false)
    }
}