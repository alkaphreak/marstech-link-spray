package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.RandomNumberResponse

fun interface RandomNumberService {

    /**
     * Generate a random number within the given range.
     *
     * @param min the minimum value (nullable string; defaults to 0 if blank)
     * @param max the maximum value (required)
     * @return a [RandomNumberResponse] containing the generated value and metadata
     */
    fun generate(min: String?, max: String?): RandomNumberResponse
}
