package fr.marstech.mtlinkspray.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Replaces @NotEmpty List<@Valid @NotBlank String>
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NotEmptyNotBlankValidator::class])
annotation class NotEmptyNotBlank(
    val message: String = "List must not be empty and contain only non-blank strings",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class NotEmptyNotBlankValidator : ConstraintValidator<NotEmptyNotBlank, List<String>> {
    override fun isValid(value: List<String>?, context: ConstraintValidatorContext?): Boolean =
        !value.isNullOrEmpty() && value.all(String::isNotBlank)
}