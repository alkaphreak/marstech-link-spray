package fr.marstech.mtlinkspray.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.net.URI
import java.net.URISyntaxException
import kotlin.reflect.KClass

/**
 * Validates that a list contains valid URLs with allowed protocols.
 *
 * Each URL in the list must:
 * - Be non-blank
 * - Be a valid URI format
 * - Have an allowed protocol (default: http, https)
 * - Not exceed the maximum length
 *
 * The list itself must not be empty.
 *
 * @property protocols Allowed URL protocols (default: ["http", "https"])
 * @property maxLength Maximum length for each URL (default: 2048)
 * @property maxSize Maximum number of URLs in the list (default: 100)
 * @property message Error message when validation fails
 */
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidUrlListValidator::class])
@MustBeDocumented
annotation class ValidUrlList(
    val message: String = "Invalid URL list",
    val protocols: Array<String> = ["http", "https"],
    val maxLength: Int = 2048,
    val maxSize: Int = 100,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

/**
 * Validator for @ValidUrlList annotation.
 *
 * Validates that:
 * 1. The list is not null or empty
 * 2. The list does not exceed the maximum size
 * 3. Each URL in the list is valid according to URL validation rules
 */
class ValidUrlListValidator : ConstraintValidator<ValidUrlList, List<String>> {

    private lateinit var allowedProtocols: Set<String>
    private var maxLength: Int = 2048
    private var maxSize: Int = 100

    override fun initialize(constraintAnnotation: ValidUrlList) {
        allowedProtocols = constraintAnnotation.protocols.map { it.lowercase() }.toSet()
        maxLength = constraintAnnotation.maxLength
        maxSize = constraintAnnotation.maxSize
    }

    override fun isValid(value: List<String>?, context: ConstraintValidatorContext?): Boolean {
        // Null or empty check
        if (value.isNullOrEmpty()) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                "URL list cannot be empty"
            )?.addConstraintViolation()
            return false
        }

        // Check list size
        if (value.size > maxSize) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                "URL list cannot contain more than $maxSize URLs"
            )?.addConstraintViolation()
            return false
        }

        // Validate each URL in the list
        value.forEachIndexed { index, url ->
            val validationError = validateUrl(url)
            if (validationError != null) {
                context?.disableDefaultConstraintViolation()
                context?.buildConstraintViolationWithTemplate(
                    "URL at index $index: $validationError"
                )?.addConstraintViolation()
                return false
            }
        }

        return true
    }

    private fun validateUrl(url: String): String? {
        // Check if blank
        if (url.isBlank()) {
            return "URL cannot be blank"
        }

        // Check length
        if (url.length > maxLength) {
            return "URL length cannot exceed $maxLength characters"
        }

        // Try to parse as URI
        val uri = try {
            URI(url)
        } catch (e: URISyntaxException) {
            return "Invalid URL format: ${e.reason}"
        } catch (_: IllegalArgumentException) {
            return "Invalid URL format"
        }

        // Check if URI has a scheme (protocol)
        val scheme = uri.scheme?.lowercase()
        if (scheme == null) {
            return "URL must have a protocol (e.g., http:// or https://)"
        }

        // Check if scheme is allowed
        if (scheme !in allowedProtocols) {
            return "URL protocol must be one of: ${allowedProtocols.joinToString(", ")}"
        }

        // Check if URI has a valid host for absolute URIs
        if (uri.isAbsolute && uri.host.isNullOrBlank()) {
            return "URL must have a valid host"
        }

        return null // No error
    }
}
