package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.RandomNumberResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import kotlin.random.Random

private const val MIN_AND_MAX_LENGTH = 9

@Validated
@RestController
@RequestMapping("/api/random")
class RandomNumberApiController {

    @Validated
    @GetMapping
    fun generateRandom(
        @RequestParam(name = "min", required = false) min: String?,
        @RequestParam(name = "max", required = false) max: String?
    ): RandomNumberResponse {

        // Input validation
        val inputMin = when {
            min.isNullOrBlank() -> 0
            (if (min.startsWith("-")) min.length - 1 else min.length) > MIN_AND_MAX_LENGTH ->
                throw IllegalArgumentException("Min length must not exceed $MIN_AND_MAX_LENGTH characters")

            else
                -> try {
                min.toInt()
            } catch (_: NumberFormatException) {
                throw IllegalArgumentException("Min value must be an integer")
            }
        }

        val inputMax = when {
            max.isNullOrBlank() -> throw MissingServletRequestParameterException("max", "int")
            max.length > MIN_AND_MAX_LENGTH -> throw IllegalArgumentException("Max length must not exceed $MIN_AND_MAX_LENGTH characters")
            else
                -> try {
                max.toInt()
            } catch (_: NumberFormatException) {
                throw IllegalArgumentException("Max value must be an integer")
            }
        }

        require(inputMin <= inputMax) { "Min value must be less than or equal to Max value" }

        // Process random generation
        return RandomNumberResponse(
            value = Random.nextInt(inputMin, inputMax + 1),
            min = inputMin,
            max = inputMax,
            timestamp = Instant.now()
        )
    }
}
