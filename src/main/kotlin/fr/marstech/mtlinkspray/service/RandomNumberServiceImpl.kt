package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.RandomNumberResponse
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.random.Random

private const val MIN_AND_MAX_LENGTH = 9

@Service
class RandomNumberServiceImpl : RandomNumberService {

    override fun generateRandom(min: String?, max: String?): RandomNumberResponse {
        val inputMin = when {
            min.isNullOrBlank() -> 0
            (if (min.startsWith("-")) min.length - 1 else min.length) >= MIN_AND_MAX_LENGTH ->
                throw IllegalArgumentException("Min length must not exceed $MIN_AND_MAX_LENGTH characters")

            else -> try {
                min.toInt()
            } catch (_: NumberFormatException) {
                throw IllegalArgumentException("Min value must be an integer")
            }
        }

        val inputMax = when {
            max.isNullOrBlank() ->
                throw IllegalArgumentException("Max value is required")

            max.length > MIN_AND_MAX_LENGTH ->
                throw IllegalArgumentException("Max length must not exceed $MIN_AND_MAX_LENGTH characters")

            else -> try {
                max.toInt()
            } catch (_: NumberFormatException) {
                throw IllegalArgumentException("Max value must be an integer")
            }
        }

        require(inputMin <= inputMax) { "Min value must be less than or equal to Max value" }

        return RandomNumberResponse(
            value = Random.nextInt(inputMin, inputMax + 1),
            min = inputMin,
            max = inputMax,
            timestamp = Instant.now()
        )
    }
}
