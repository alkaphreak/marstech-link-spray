package fr.marstech.mtlinkspray.domain.devtools
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.random.Random

private const val MIN_AND_MAX_LENGTH = 9

@Service
class RandomNumberService {

    /**
     * Generate a random number within the given range.
     *
     * @param min the minimum value (nullable string; defaults to 0 if blank)
     * @param max the maximum value (required)
     * @return a [RandomNumber] containing the generated value and metadata
     */
    fun generate(min: String?, max: String?): RandomNumber {
        val inputMin = when {
            min.isNullOrBlank() -> 0
            min == "-" || (if (min.startsWith("-")) min.length - 1 else min.length) >= MIN_AND_MAX_LENGTH ->
                throw IllegalArgumentException("Min length must not exceed $MIN_AND_MAX_LENGTH characters")
            else -> try {
                min.toInt()
            } catch (_: NumberFormatException) {
                throw IllegalArgumentException("Min value must be an integer")
            }
        }

        val inputMax = when {
            max.isNullOrBlank() ->
                // Same message as Spring MVC for a missing @RequestParam (GET /api/random; MCP refuses a missing max by its schema)
                throw IllegalArgumentException("Required request parameter 'max' for method parameter type int is not present")
            max.length > MIN_AND_MAX_LENGTH ->
                throw IllegalArgumentException("Max length must not exceed $MIN_AND_MAX_LENGTH characters")
            else -> try {
                max.toInt()
            } catch (_: NumberFormatException) {
                throw IllegalArgumentException("Max value must be an integer")
            }
        }

        require(inputMin <= inputMax) { "Min value must be less than or equal to Max value" }

        return RandomNumber(
            value = Random.nextInt(inputMin, inputMax + 1),
            min = inputMin,
            max = inputMax,
            timestamp = Instant.now()
        )
    }
}
