package fr.marstech.mtlinkspray.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.bind.MissingServletRequestParameterException

class RandomNumberServiceImplTest {

    private lateinit var service: RandomNumberServiceImpl

    @BeforeEach
    fun setUp() {
        service = RandomNumberServiceImpl()
    }

    // --- Happy path ---

    @Test
    fun generateShouldReturnValueWithinRange() {
        // Given
        val min = "5"
        val max = "10"

        // When
        val response = service.generate(min, max)

        // Then
        assertTrue(response.value in 5..10)
        assertEquals(5, response.min)
        assertEquals(10, response.max)
        assertNotNull(response.timestamp)
    }

    @Test
    fun generateShouldDefaultMinToZeroWhenMinIsNull() {
        // Given / When
        val response = service.generate(null, "50")

        // Then
        assertEquals(0, response.min)
        assertEquals(50, response.max)
        assertTrue(response.value in 0..50)
    }

    @Test
    fun generateShouldDefaultMinToZeroWhenMinIsBlank() {
        // Given / When
        val response = service.generate("  ", "50")

        // Then
        assertEquals(0, response.min)
        assertEquals(50, response.max)
        assertTrue(response.value in 0..50)
    }

    @Test
    fun generateShouldReturnMinWhenMinEqualsMax() {
        // Given / When
        val response = service.generate("7", "7")

        // Then
        assertEquals(7, response.value)
        assertEquals(7, response.min)
        assertEquals(7, response.max)
    }

    @Test
    fun generateShouldAcceptNegativeMin() {
        // Given / When
        val response = service.generate("-10", "10")

        // Then
        assertEquals(-10, response.min)
        assertEquals(10, response.max)
        assertTrue(response.value in -10..10)
    }

    @Test
    fun generateShouldAcceptBoundaryValuesWithNineSignificantDigits() {
        // Given - values within the 9-character significant digit limit
        val response = service.generate("-10000000", "100000000")

        // Then
        assertEquals(-10000000, response.min)
        assertEquals(100000000, response.max)
    }

    // --- Missing / blank max ---

    @Test
    fun generateShouldThrowMissingParameterExceptionWhenMaxIsNull() {
        // Given / When / Then
        assertThrows(MissingServletRequestParameterException::class.java) {
            service.generate("0", null)
        }
    }

    @Test
    fun generateShouldThrowMissingParameterExceptionWhenMaxIsBlank() {
        // Given / When / Then
        assertThrows(MissingServletRequestParameterException::class.java) {
            service.generate("0", "  ")
        }
    }

    // --- Min validation ---

    @Test
    fun generateShouldThrowWhenMinIsSingleDash() {
        // Given / When / Then
        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.generate("-", "10")
        }
        assertTrue(ex.message!!.contains("Min length"))
    }

    @Test
    fun generateShouldThrowWhenMinExceedsNineCharacters() {
        // Given - 10 digits exceeds the 9-character limit
        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.generate("1000000000", "2000000000")
        }
        assertTrue(ex.message!!.contains("Min length"))
    }

    @Test
    fun generateShouldThrowWhenMinIsNotANumber() {
        // Given / When / Then
        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.generate("abc", "10")
        }
        assertTrue(ex.message!!.contains("Min value must be an integer"))
    }

    // --- Max validation ---

    @Test
    fun generateShouldThrowWhenMaxExceedsNineCharacters() {
        // Given - 10 digits exceeds the 9-character limit
        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.generate("0", "1000000000")
        }
        assertTrue(ex.message!!.contains("Max length"))
    }

    @Test
    fun generateShouldThrowWhenMaxIsNotANumber() {
        // Given / When / Then
        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.generate("0", "xyz")
        }
        assertTrue(ex.message!!.contains("Max value must be an integer"))
    }

    // --- Range constraint ---

    @Test
    fun generateShouldThrowWhenMinIsGreaterThanMax() {
        // Given / When / Then
        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.generate("100", "10")
        }
        assertTrue(ex.message!!.contains("Min value must be less than or equal to Max value"))
    }
}