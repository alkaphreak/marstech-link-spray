package fr.marstech.mtlinkspray.controller.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalRestExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), HttpStatus.INTERNAL_SERVER_ERROR)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalAccessException::class)
    fun handleUnauthorized(e: IllegalAccessException): ResponseEntity<Map<String, String?>> =
        ResponseEntity(mapOf("error" to e.message), HttpStatus.UNAUTHORIZED)
}
