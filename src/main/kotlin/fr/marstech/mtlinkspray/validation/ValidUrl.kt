package fr.marstech.mtlinkspray.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.net.URI
import java.net.URISyntaxException
import kotlin.reflect.KClass

/**
 * Validator for @ValidUrl annotation.
 *
 * Validates that:
 * 1. The string is not blank
 * 2. The string is a valid URI format
 * 3. The URI has an allowed protocol (scheme)
 * 4. The URI length does not exceed the maximum
 * 5. The URI has a valid host (not null/empty for absolute URIs)
 */
class ValidUrlValidator : ConstraintValidator<ValidUrl, String> {

    private lateinit var allowedProtocols: Set<String>
    private var maxLength: Int = 2048

    override fun initialize(constraintAnnotation: ValidUrl) {
        allowedProtocols = constraintAnnotation.protocols.map { it.lowercase() }.toSet()
        maxLength = constraintAnnotation.maxLength
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        // @NotBlank should handle null or blank values
        if (value.isNullOrBlank()) {
            return true
        }

        // Check length
        if (value.length > maxLength) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                "URL length cannot exceed $maxLength characters"
            )?.addConstraintViolation()
            return false
        }

        // Try to parse as URI
        val uri = try {
            URI(value)
        } catch (e: URISyntaxException) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                "Invalid URL format: ${e.reason}"
            )?.addConstraintViolation()
            return false
        } catch (_: IllegalArgumentException) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                "Invalid URL format"
            )?.addConstraintViolation()
            return false
        }

        // Check if URI has a scheme (protocol)
        val scheme = uri.scheme?.lowercase()
        if (scheme == null) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                "URL must have a protocol (e.g., http:// or https://)"
            )?.addConstraintViolation()
            return false
        }

        // Check if the scheme is allowed
        if (scheme !in allowedProtocols) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                "URL protocol must be one of: ${allowedProtocols.joinToString(", ")}"
            )?.addConstraintViolation()
            return false
        }

        // Check if URI has a valid host for absolute URIs
        if (uri.isAbsolute && uri.host.isNullOrBlank()) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                "URL must have a valid host"
            )?.addConstraintViolation()
            return false
        }

        return true
    }
}

/**
 * Validates that a string is a valid URL with allowed protocols.
 *
 * By default, only HTTP and HTTPS protocols are allowed.
 * The maximum URL length is 2048 characters.
 *
 * @property protocols Allowed URL protocols (default: ["http", "https"])
 * @property message Error message when validation fails
 */
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidUrlValidator::class])
@MustBeDocumented
annotation class ValidUrl(
    val message: String = "Invalid URL format or protocol",
    val protocols: Array<String> = ["http", "https"],
    val maxLength: Int = 2048,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
