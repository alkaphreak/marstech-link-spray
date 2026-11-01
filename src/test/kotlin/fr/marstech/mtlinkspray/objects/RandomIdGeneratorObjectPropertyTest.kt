package fr.marstech.mtlinkspray.objects

import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import net.jqwik.api.constraints.StringLength
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.Random

/**
 * Property-based checks of the random id generator (MARSTECH-847): length and alphabet hold for
 * any length, charset and prefix, not just the default ones.
 */
class RandomIdGeneratorObjectPropertyTest {

    @Property(seed = "847")
    fun shouldRespectLengthAndCharset(
        @ForAll @IntRange(min = 0, max = 200) length: Int,
        @ForAll @StringLength(min = 1, max = 40) charset: String,
        @ForAll seed: Long
    ) {
        // Given a length, a charset and a seeded Random
        // When
        val id = RandomIdGeneratorObject.generate(length, charset.toCharArray(), Random(seed))
        // Then
        assertEquals(length, id.length)
        assertTrue(id.all { it in charset }, "unexpected char in $id")
    }

    @Property(seed = "847")
    fun shouldKeepTrimmedPrefixAndTotalLength(
        @ForAll @StringLength(max = 10) prefix: String,
        @ForAll @IntRange(min = 0, max = 50) extra: Int,
        @ForAll seed: Long
    ) {
        // Given a prefix and a total length at least as long as the trimmed prefix
        val trimmed = prefix.trim()
        val totalLength = trimmed.length + extra
        // When
        val id = RandomIdGeneratorObject.generate(prefix, totalLength, random = Random(seed))
        // Then
        assertTrue(id.startsWith(trimmed))
        assertEquals(totalLength, id.length)
        assertTrue(id.drop(trimmed.length).all { it in RandomIdGeneratorObject.DEFAULT_CHARSET })
    }
}
