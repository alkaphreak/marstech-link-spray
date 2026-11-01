package fr.marstech.mtlinkspray.utils

import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.Provide
import net.jqwik.api.constraints.Size
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Property-based checks of the image upload gate (MARSTECH-847): invariants that must hold for
 * any byte sequence, not just a few hand-picked fixtures.
 */
class ImageTypesPropertyTest {

    @Property(seed = "847")
    fun shouldOnlyEverDetectAllowedTypes(@ForAll @Size(max = 64) bytes: ByteArray) {
        // Given any byte sequence
        // When
        val detected = ImageTypes.detect(bytes)
        // Then
        assertTrue(detected == null || detected in ImageTypes.ALLOWED, "detected $detected")
    }

    @Property(seed = "847")
    fun shouldNeverAcceptSvgOrXmlContent(
        @ForAll("markupPrefixes") prefix: String,
        @ForAll @Size(max = 64) suffix: ByteArray
    ) {
        // Given content starting like an SVG or XML document
        val bytes = prefix.toByteArray() + suffix
        // When / Then
        assertNull(ImageTypes.detect(bytes))
    }

    @Property(seed = "847")
    fun shouldDetectTypeFromHeaderWhateverFollows(
        @ForAll("knownHeaders") header: Pair<ByteArray, String>,
        @ForAll @Size(max = 64) suffix: ByteArray
    ) {
        // Given a valid magic-byte header followed by arbitrary bytes
        val bytes = header.first + suffix
        // When / Then
        assertEquals(header.second, ImageTypes.detect(bytes))
    }

    @Provide
    fun markupPrefixes(): Arbitrary<String> = Arbitraries.of("<svg", "<?xml", "<!DOCTYPE svg")

    @Provide
    fun knownHeaders(): Arbitrary<Pair<ByteArray, String>> = Arbitraries.of(
        bytesOf(0xFF, 0xD8, 0xFF) to "image/jpeg",
        bytesOf(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) to "image/png",
        "GIF89a".toByteArray() to "image/gif",
        "RIFF".toByteArray() + bytesOf(0, 0, 0, 0) + "WEBP".toByteArray() to "image/webp"
    )

    private fun bytesOf(vararg values: Int): ByteArray = ByteArray(values.size) { values[it].toByte() }
}
