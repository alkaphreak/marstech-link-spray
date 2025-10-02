package fr.marstech.mtlinkspray.objects

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RandomIdGeneratorObjectTest {

    @Test
    fun getDefaultCharset() {
        val charset = RandomIdGeneratorObject.DEFAULT_CHARSET
        assertTrue(charset.contains('a'))
        assertTrue(charset.contains('Z'))
        assertTrue(charset.contains('0'))
        assertEquals(62, charset.length)
    }

    @Test
    fun generateDefault() {
        val id = RandomIdGeneratorObject.generate()
        assertEquals(RandomIdGeneratorObject.DEFAULT_SIZE, id.length)
        assertTrue(id.all { RandomIdGeneratorObject.DEFAULT_CHARSET.contains(it) })
    }

    @Test
    fun generateWithPrefixAndLength() {
        val prefix = "abc"
        val totalLength = 10
        val id = RandomIdGeneratorObject.generate(prefix, totalLength)
        assertTrue(id.startsWith(prefix))
        assertEquals(totalLength, id.length)
        assertTrue(id.drop(prefix.length).all { RandomIdGeneratorObject.DEFAULT_CHARSET.contains(it) })
    }

    @Test
    fun generateWithCustomCharset() {
        val charset = "XYZ"
        val id = RandomIdGeneratorObject.generate("", 5, charset)
        assertEquals(5, id.length)
        assertTrue(id.all { charset.contains(it) })
    }

    @Test
    fun generatePrefixTrimmed() {
        val id = RandomIdGeneratorObject.generate("  ab  ", 6)
        assertTrue(id.startsWith("ab"))
        assertEquals(6, id.length)
    }

    @Test
    fun generateTotalLengthLessThanPrefixThrows() {
        assertThrows<IllegalArgumentException> {
            RandomIdGeneratorObject.generate("prefix", 2)
        }
    }

    @Test
    fun generateZeroLength() {
        val id = RandomIdGeneratorObject.generate("", 0)
        assertEquals(0, id.length)
    }
}