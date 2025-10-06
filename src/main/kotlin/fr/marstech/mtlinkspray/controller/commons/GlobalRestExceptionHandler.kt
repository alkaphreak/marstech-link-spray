package fr.marstech.mtlinkspray.controller.commons

import jakarta.validation.ConstraintViolationException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
class GlobalRestExceptionHandler {

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to (ex.message ?: "Internal server error")))

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), INTERNAL_SERVER_ERROR)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), BAD_REQUEST)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), NOT_FOUND)

    @ExceptionHandler(IllegalAccessException::class)
    fun handleUnauthorized(e: IllegalAccessException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), UNAUTHORIZED)

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(e: MissingServletRequestParameterException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), BAD_REQUEST)

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationException(e: ConstraintViolationException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), BAD_REQUEST)

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: org.springframework.web.bind.MethodArgumentNotValidException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), BAD_REQUEST)

    @ExceptionHandler(org.springframework.validation.BindException::class)
    fun handleBindException(e: org.springframework.validation.BindException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), BAD_REQUEST)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(e: HttpMessageNotReadableException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), BAD_REQUEST)
}