package fr.marstech.mtlinkspray.controller.commons

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GlobalRestExceptionHandlerTest {

    private val handler = GlobalRestExceptionHandler()

    @Test
    fun `handleRuntimeException returns 500 and error message`() {
        val ex = RuntimeException("runtime error")
        val response = handler.handleRuntimeException(ex)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("runtime error", response.body?.get("error"))
    }

    @Test
    fun `handleException returns 500 and error message`() {
        val ex = Exception("general error")
        val response = handler.handleException(ex)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("general error", response.body?.get("error"))
    }

    @Test
    fun `handleIllegalArgument returns 400 and error message`() {
        val ex = IllegalArgumentException("bad argument")
        val response = handler.handleIllegalArgument(ex)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("bad argument", response.body?.get("error"))
    }

    @Test
    fun `handleNotFound returns 404 and error message`() {
        val ex = NoSuchElementException("not found")
        val response = handler.handleNotFound(ex)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("not found", response.body?.get("error"))
    }

    @Test
    fun `handleUnauthorized returns 401 and error message`() {
        val ex = IllegalAccessException("unauthorized")
        val response = handler.handleUnauthorized(ex)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("unauthorized", response.body?.get("error"))
    }
}