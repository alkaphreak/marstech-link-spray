package fr.marstech.mtlinkspray.objects

import java.util.*

object RandomIdGeneratorObject {

    private val DEFAULT_CHARSET: String = (('a'..'z') + ('A'..'Z') + ('0'..'9')).joinToString("")

    private const val DEFAULT_SIZE: Int = 6

    private val random = Random()

    private fun generate(length: Int = DEFAULT_SIZE, charset: String = DEFAULT_CHARSET): String = buildString {
        repeat(length) { append(charset[random.nextInt(charset.length)]) }
    }

    fun generate(
        prefix: String,
        totalLength: Int = DEFAULT_SIZE,
        charset: String = DEFAULT_CHARSET
    ): String = prefix.trim().let { it + generate(length = totalLength - it.length, charset = charset) }

    fun generate(): String = generate("", DEFAULT_SIZE, DEFAULT_CHARSET)
}
