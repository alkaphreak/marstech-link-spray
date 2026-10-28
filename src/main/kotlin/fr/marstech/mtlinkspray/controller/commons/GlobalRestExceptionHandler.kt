package fr.marstech.mtlinkspray.controller.commons

import fr.marstech.mtlinkspray.dto.ErrorResponse
import fr.marstech.mtlinkspray.dto.FieldError
import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import jakarta.validation.ConstraintViolationException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.method.MethodValidationException
import org.springframework.validation.method.MethodValidationResult
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.accept.InvalidApiVersionException
import org.springframework.web.accept.MissingApiVersionException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.beans.TypeMismatchException
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.unit.DataSize
import org.springframework.core.convert.ConversionFailedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.resource.NoResourceFoundException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartException
import org.springframework.web.multipart.support.MissingServletRequestPartException

private const val VALIDATION_FAILED = "Validation Failed"

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
class GlobalRestExceptionHandler(
    @Value("\${spring.servlet.multipart.max-file-size:25MB}") private val maxFileSize: DataSize = DataSize.ofMegabytes(25),
) {

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
        // Spring MVC's own exceptions (415, 405, 406, ...) carry their status: keep it instead
        // of turning a client error into a 500
        val status = (e as? org.springframework.web.ErrorResponse)?.statusCode
            ?.let { HttpStatus.resolve(it.value()) } ?: INTERNAL_SERVER_ERROR
        val errorResponse = ErrorResponse.from(
            status = status.value(),
            error = status.reasonPhrase,
            message = e.message ?: "An unexpected error occurred",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, status)
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

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(
        e: NoResourceFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = NOT_FOUND.value(),
            error = "Not Found",
            message = "The requested resource does not exist",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, NOT_FOUND)
    }

    @ExceptionHandler(PayloadTooLargeException::class)
    fun handlePayloadTooLarge(
        e: PayloadTooLargeException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = CONTENT_TOO_LARGE.value(),
            error = "Content Too Large",
            message = e.message ?: "Request body too large",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, CONTENT_TOO_LARGE)
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

    // Unsupported or malformed API version (MLS-137); NotAcceptableApiVersionException is a subclass
    @ExceptionHandler(InvalidApiVersionException::class, MissingApiVersionException::class)
    fun handleApiVersion(
        e: ResponseStatusException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = BAD_REQUEST.value(),
            error = "Bad Request",
            message = e.reason ?: "Invalid API version",
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
            error = VALIDATION_FAILED,
            message = "Constraint violation",
            fieldErrors = fieldErrors,
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, BAD_REQUEST)
    }

    // Constraints on @RequestParam/@PathVariable: MethodValidationException from the @Validated proxy
    // (ValidationConfig adapts constraint violations), HandlerMethodValidationException from Spring MVC
    @ExceptionHandler(MethodValidationException::class, HandlerMethodValidationException::class)
    fun handleMethodValidation(
        e: MethodValidationResult,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.parameterValidationResults.flatMap { result ->
            // Report the name the client sent (e.g. "url"), not the Kotlin parameter name
            val field = result.methodParameter.getParameterAnnotation(RequestParam::class.java)
                ?.name?.ifEmpty { null } ?: result.methodParameter.parameterName ?: "argument"
            result.resolvableErrors.map { error ->
                FieldError(
                    field = field,
                    rejectedValue = result.argument,
                    message = error.defaultMessage ?: "Validation failed"
                )
            }
        }

        val errorResponse = ErrorResponse.withFieldErrors(
            status = BAD_REQUEST.value(),
            error = VALIDATION_FAILED,
            message = "Invalid request parameters",
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
            error = VALIDATION_FAILED,
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

    // Whole megabytes read as "25 MB"; any other limit is given in exact bytes rather than truncated.
    private val uploadLimit = maxFileSize.toBytes().let { bytes ->
        if (bytes % (1024 * 1024) == 0L) "${bytes / (1024 * 1024)} MB" else "$bytes bytes"
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceeded(
        e: MaxUploadSizeExceededException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = CONTENT_TOO_LARGE.value(),
            error = "Content Too Large",
            message = "File exceeds the maximum upload size of $uploadLimit",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity(errorResponse, CONTENT_TOO_LARGE)
    }

    @ExceptionHandler(MultipartException::class, MissingServletRequestPartException::class)
    fun handleMultipart(
        e: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(
            status = BAD_REQUEST.value(),
            error = "Bad Request",
            message = e.message ?: "Invalid multipart request",
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