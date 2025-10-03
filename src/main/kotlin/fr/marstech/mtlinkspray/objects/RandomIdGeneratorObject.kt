package fr.marstech.mtlinkspray.objects

import java.security.SecureRandom
import java.util.Random

/**
 * Utility object for generating random IDs with optional prefix and custom charset.
 * Thread-safe: uses SecureRandom.
 */
object RandomIdGeneratorObject {

    private val secureRandom: Random = SecureRandom()

    /** Default character set: a-z, A-Z, 0-9 */
    const val DEFAULT_SIZE: Int = 6
    val DEFAULT_CHARSET: CharArray = (('a'..'z') + ('A'..'Z') + ('0'..'9')).toCharArray()

    /**
     * Generates a random string of the given length and charset.
     * @param length Length of the random part (must be >= 0)
     * @param charset Characters to use for random generation
     * @param random Random instance (for testing, defaults to SecureRandom)
     * @return Random string
     */
    @JvmOverloads
    fun generate(
        length: Int = DEFAULT_SIZE,
        charset: CharArray = DEFAULT_CHARSET,
        random: Random = secureRandom
    ): String {
        require(length >= 0) { "Length must be non-negative" }
        return buildString {
            repeat(length) { append(charset[random.nextInt(charset.size)]) }
        }
    }

    /**
     * Generates a random ID with a prefix, total length, and charset.
     * @param prefix Prefix for the ID (trimmed)
     * @param totalLength Total length of the ID (must be >= prefix.length)
     * @param charset Characters to use for random generation
     * @param random Random instance (for testing, defaults to SecureRandom)
     * @return Random ID string
     */
    @JvmOverloads
    fun generate(
        prefix: String,
        totalLength: Int = DEFAULT_SIZE,
        charset: CharArray = DEFAULT_CHARSET,
        random: Random = secureRandom
    ): String {
        val trimmedPrefix = prefix.trim()
        require(totalLength >= trimmedPrefix.length) { "totalLength must be >= prefix.length" }
        return trimmedPrefix + generate(
            length = totalLength - trimmedPrefix.length,
            charset = charset,
            random = random
        )
    }
}