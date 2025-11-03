package fr.marstech.mtlinkspray.controller.commons

import jakarta.validation.ConstraintViolationException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.beans.TypeMismatchException
import org.springframework.core.convert.ConversionFailedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
class GlobalRestExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to (e.message ?: "Invalid argument type")), BAD_REQUEST)

    @ExceptionHandler(TypeMismatchException::class)
    fun handleTypeMismatch(e: TypeMismatchException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to (e.message ?: "Type mismatch")), BAD_REQUEST)

    @ExceptionHandler(ConversionFailedException::class)
    fun handleConversionFailed(e: ConversionFailedException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to (e.message ?: "Conversion failed")), BAD_REQUEST)

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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), BAD_REQUEST)

    @ExceptionHandler(BindException::class)
    fun handleBindException(e: BindException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), BAD_REQUEST)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(e: HttpMessageNotReadableException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), BAD_REQUEST)
}