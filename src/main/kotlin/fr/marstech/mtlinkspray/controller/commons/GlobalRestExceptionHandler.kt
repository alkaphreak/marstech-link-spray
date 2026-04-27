package fr.marstech.mtlinkspray.controller.commons

import fr.marstech.mtlinkspray.dto.ErrorResponse
import fr.marstech.mtlinkspray.dto.FieldError
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
import org.springframework.web.context.request.WebRequest

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
class GlobalRestExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        e: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = BAD_REQUEST.value(),
            error = "Bad Request",
            message = e.message ?: "Invalid argument type",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, BAD_REQUEST)
    }

    @ExceptionHandler(TypeMismatchException::class)
    fun handleTypeMismatch(
        e: TypeMismatchException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = BAD_REQUEST.value(),
            error = "Bad Request",
            message = e.message ?: "Type mismatch",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, BAD_REQUEST)
    }

    @ExceptionHandler(ConversionFailedException::class)
    fun handleConversionFailed(
        e: ConversionFailedException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = BAD_REQUEST.value(),
            error = "Bad Request",
            message = e.message ?: "Conversion failed",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, BAD_REQUEST)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "Internal server error",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = e.message ?: "An unexpected error occurred",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        e: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = BAD_REQUEST.value(),
            error = "Bad Request",
            message = e.message ?: "Invalid argument",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, BAD_REQUEST)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(
        e: NoSuchElementException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = NOT_FOUND.value(),
            error = "Not Found",
            message = e.message ?: "Resource not found",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, NOT_FOUND)
    }

    @ExceptionHandler(IllegalAccessException::class)
    fun handleUnauthorized(
        e: IllegalAccessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = e.message ?: "Access denied",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, UNAUTHORIZED)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(
        e: MissingServletRequestParameterException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = BAD_REQUEST.value(),
            error = "Bad Request",
            message = e.message ?: "Missing required parameter",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationException(
        e: ConstraintViolationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.constraintViolations.map { violation ->
            FieldError(
                field = violation.propertyPath.toString(),
                rejectedValue = violation.invalidValue,
                message = violation.message
            )
        }

        val errorResponse = ErrorResponse.withFieldErrors(
            status = BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Constraint violation",
            fieldErrors = fieldErrors,
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        e: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.bindingResult.fieldErrors.map { fieldError ->
            FieldError(
                field = fieldError.field,
                rejectedValue = fieldError.rejectedValue,
                message = fieldError.defaultMessage ?: "Validation failed"
            )
        }

        val errorResponse = ErrorResponse.withFieldErrors(
            status = BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Invalid request data",
            fieldErrors = fieldErrors,
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity(errorResponse, BAD_REQUEST)
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(
        e: BindException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.fieldErrors.map { fieldError ->
            FieldError(
                field = fieldError.field,
                rejectedValue = fieldError.rejectedValue,
                message = fieldError.defaultMessage ?: "Binding failed"
            )
        }

        val errorResponse = ErrorResponse.withFieldErrors(
            status = BAD_REQUEST.value(),
            error = "Binding Failed",
            message = "Invalid request data",
            fieldErrors = fieldErrors,
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(
        e: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = BAD_REQUEST.value(),
            error = "Bad Request",
            message = e.message ?: "Malformed JSON request",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, BAD_REQUEST)
    }
}