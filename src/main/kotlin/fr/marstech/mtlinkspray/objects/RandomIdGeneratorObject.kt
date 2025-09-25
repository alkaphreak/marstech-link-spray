package fr.marstech.mtlinkspray.objects

import java.util.concurrent.ThreadLocalRandom

/**
 * Utility object for generating random IDs with optional prefix and custom charset.
 */
object RandomIdGeneratorObject {

    /** Default character set: a-z, A-Z, 0-9 */
    const val DEFAULT_SIZE: Int = 6
    val DEFAULT_CHARSET: String = (('a'..'z') + ('A'..'Z') + ('0'..'9')).joinToString("")

    /**
     * Generates a random string of the given length and charset.
     * @param length Length of the random part (must be >= 0)
     * @param charset Characters to use for random generation
     * @return Random string
     */
    fun generate(length: Int = DEFAULT_SIZE, charset: String = DEFAULT_CHARSET): String = buildString {
        require(length >= 0) { "Length must be non-negative" }
        repeat(times = length) {
            append(charset[ThreadLocalRandom.current().nextInt(charset.length)])
        }
    }

    /**
     * Generates a random ID with a prefix, total length, and charset.
     * @param prefix Prefix for the ID (trimmed)
     * @param totalLength Total length of the ID (must be >= prefix.length)
     * @param charset Characters to use for random generation
     * @return Random ID string
     */
    fun generate(
        prefix: String = "", totalLength: Int = DEFAULT_SIZE, charset: String = DEFAULT_CHARSET
    ): String {
        val trimmedPrefix = prefix.trim()
        require(totalLength >= trimmedPrefix.length) { "totalLength must be >= prefix.length" }
        return trimmedPrefix + generate(length = totalLength - trimmedPrefix.length, charset = charset)
    }

    /**
     * Generates a random ID of default size and charset, no prefix.
     * @return Random ID string
     */
    fun generate(): String = generate(prefix = "", totalLength = DEFAULT_SIZE, charset = DEFAULT_CHARSET)
}
