package fr.marstech.mtlinkspray.dto

import java.time.LocalDateTime

/**
 * Structured error response for API validation and error handling
 *
 * @property timestamp When the error occurred
 * @property status HTTP status code
 * @property error Error type (e.g., "Bad Request")
 * @property message General error message
 * @property path Request path that caused the error
 * @property fieldErrors List of field-specific validation errors (optional)
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val fieldErrors: List<FieldError>? = null
) {
    companion object {
        /**
         * Create an ErrorResponse from a simple error message
         */
        fun from(status: Int, error: String, message: String, path: String? = null): ErrorResponse {
            return ErrorResponse(
                status = status,
                error = error,
                message = message,
                path = path
            )
        }

        /**
         * Create an ErrorResponse with field-specific validation errors
         */
        fun withFieldErrors(
            status: Int,
            error: String,
            message: String,
            fieldErrors: List<FieldError>,
            path: String? = null
        ): ErrorResponse {
            return ErrorResponse(
                status = status,
                error = error,
                message = message,
                path = path,
                fieldErrors = fieldErrors
            )
        }
    }
}

/**
 * Field-specific validation error
 *
 * @property field Name of the field that failed validation
 * @property rejectedValue The value that was rejected (can be null)
 * @property message Validation error message
 */
data class FieldError(
    val field: String,
    val rejectedValue: Any?,
    val message: String
)
