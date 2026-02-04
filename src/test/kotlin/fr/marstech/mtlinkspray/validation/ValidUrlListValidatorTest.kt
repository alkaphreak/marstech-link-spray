package fr.marstech.mtlinkspray.validation

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("ValidUrlList Annotation Tests")
class ValidUrlListValidatorTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    // Test data class for validation
    data class TestDto(
        @field:ValidUrlList
        val urls: List<String>?
    )

    data class TestDtoWithCustomMaxSize(
        @field:ValidUrlList(maxSize = 3)
        val urls: List<String>?
    )

    data class TestDtoWithCustomMaxLength(
        @field:ValidUrlList(maxLength = 50)
        val urls: List<String>?
    )

    data class TestDtoWithCustomProtocols(
        @field:ValidUrlList(protocols = ["ftp", "sftp"])
        val urls: List<String>?
    )

    @Test
    @DisplayName("Should accept list with valid HTTP URLs")
    fun shouldAcceptListWithValidHttpUrls() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "http://example.com",
                "http://test.com",
                "http://demo.org"
            )
        )

        // When
        val violations: Set<ConstraintViolation<TestDto>> = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Valid HTTP URLs should not produce violations")
    }

    @Test
    @DisplayName("Should accept list with valid HTTPS URLs")
    fun shouldAcceptListWithValidHttpsUrls() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "https://example.com",
                "https://test.com/path",
                "https://demo.org?param=value"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Valid HTTPS URLs should not produce violations")
    }

    @Test
    @DisplayName("Should accept list with mixed HTTP and HTTPS URLs")
    fun shouldAcceptListWithMixedProtocols() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "http://example.com",
                "https://test.com",
                "http://demo.org"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Mixed HTTP/HTTPS URLs should not produce violations")
    }

    @Test
    @DisplayName("Should accept single URL in list")
    fun shouldAcceptSingleUrlInList() {
        // Given
        val dto = TestDto(urls = listOf("https://example.com"))

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Single valid URL should not produce violations")
    }

    @Test
    @DisplayName("Should reject empty list")
    fun shouldRejectEmptyList() {
        // Given
        val dto = TestDto(urls = emptyList())

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "Empty list should produce violations")
        assertTrue(
            violations.any { it.message.contains("cannot be empty") },
            "Error message should mention empty list"
        )
    }

    @Test
    @DisplayName("Should reject null list")
    fun shouldRejectNullList() {
        // Given
        val dto = TestDto(urls = null)

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "Null list should produce violations")
    }

    @Test
    @DisplayName("Should reject list with URL without protocol")
    fun shouldRejectListWithUrlWithoutProtocol() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "https://valid.com",
                "invalid.com",
                "https://another-valid.com"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "List with URL without protocol should produce violations")
        assertTrue(
            violations.any { it.message.contains("index 1") && it.message.contains("protocol") },
            "Error message should mention index and protocol requirement"
        )
    }

    @Test
    @DisplayName("Should reject list with URL with invalid protocol")
    fun shouldRejectListWithUrlWithInvalidProtocol() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "https://valid.com",
                "ftp://invalid-protocol.com"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "List with invalid protocol should produce violations")
        assertTrue(
            violations.any { it.message.contains("index 1") && it.message.contains("protocol must be one of") },
            "Error message should mention index and allowed protocols"
        )
    }

    @Test
    @DisplayName("Should reject list with blank URL")
    fun shouldRejectListWithBlankUrl() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "https://valid.com",
                "   ",
                "https://another-valid.com"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "List with blank URL should produce violations")
        assertTrue(
            violations.any { it.message.contains("index 1") && it.message.contains("blank") },
            "Error message should mention index and blank URL"
        )
    }

    @Test
    @DisplayName("Should reject list with malformed URL")
    fun shouldRejectListWithMalformedUrl() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "https://valid.com",
                "http://[invalid",
                "https://another-valid.com"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "List with malformed URL should produce violations")
        assertTrue(
            violations.any { it.message.contains("index 1") && it.message.contains("Invalid URL format") },
            "Error message should mention index and invalid format"
        )
    }

    @Test
    @DisplayName("Should reject list exceeding max size")
    fun shouldRejectListExceedingMaxSize() {
        // Given
        val dto = TestDtoWithCustomMaxSize(
            urls = listOf(
                "https://url1.com",
                "https://url2.com",
                "https://url3.com",
                "https://url4.com"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "List exceeding max size should produce violations")
        assertTrue(
            violations.any { it.message.contains("cannot contain more than 3") },
            "Error message should mention max size"
        )
    }

    @Test
    @DisplayName("Should accept list at max size boundary")
    fun shouldAcceptListAtMaxSizeBoundary() {
        // Given
        val dto = TestDtoWithCustomMaxSize(
            urls = listOf(
                "https://url1.com",
                "https://url2.com",
                "https://url3.com"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "List at max size boundary should not produce violations")
    }

    @Test
    @DisplayName("Should reject URL in list exceeding max length")
    fun shouldRejectUrlInListExceedingMaxLength() {
        // Given
        val longUrl = "http://example.com/" + "a".repeat(100)
        val dto = TestDtoWithCustomMaxLength(
            urls = listOf(
                "http://short.com",
                longUrl
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "List with URL exceeding max length should produce violations")
        assertTrue(
            violations.any { it.message.contains("index 1") && it.message.contains("length cannot exceed") },
            "Error message should mention index and length limit"
        )
    }

    @Test
    @DisplayName("Should accept URLs at max length boundary")
    fun shouldAcceptUrlsAtMaxLengthBoundary() {
        // Given
        val url = "http://example.com/" + "a".repeat(28) // Total = 50 chars
        val dto = TestDtoWithCustomMaxLength(
            urls = listOf(url)
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "URL at max length boundary should not produce violations")
    }

    @Test
    @DisplayName("Should accept list with custom protocols")
    fun shouldAcceptListWithCustomProtocols() {
        // Given
        val dto = TestDtoWithCustomProtocols(
            urls = listOf(
                "ftp://example.com",
                "sftp://test.com"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "List with custom allowed protocols should not produce violations")
    }

    @Test
    @DisplayName("Should reject list with protocol not in custom list")
    fun shouldRejectListWithProtocolNotInCustomList() {
        // Given
        val dto = TestDtoWithCustomProtocols(
            urls = listOf(
                "ftp://valid.com",
                "http://invalid.com"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "List with protocol not in custom list should produce violations")
    }

    @Test
    @DisplayName("Should accept URLs with complex paths and query strings")
    fun shouldAcceptUrlsWithComplexPathsAndQueryStrings() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "https://example.com/path/to/resource?param1=value1&param2=value2",
                "http://test.com/another/path?query=test#fragment",
                "https://api.example.org/v1/users/123?include=details&format=json"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "URLs with complex paths and queries should not produce violations")
    }

    @Test
    @DisplayName("Should accept URLs with ports")
    fun shouldAcceptUrlsWithPorts() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "http://localhost:8080",
                "https://example.com:443/path",
                "http://192.168.1.1:3000"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "URLs with ports should not produce violations")
    }

    @Test
    @DisplayName("Should report first invalid URL only")
    fun shouldReportFirstInvalidUrlOnly() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "https://valid.com",
                "invalid1",
                "invalid2",
                "https://another-valid.com"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "Should produce violations")
        assertTrue(
            violations.any { it.message.contains("index 1") },
            "Should report first invalid URL at index 1"
        )
    }

    @Test
    @DisplayName("Should accept large list under max size")
    fun shouldAcceptLargeListUnderMaxSize() {
        // Given
        val urls = (1..50).map { "https://example$it.com" }
        val dto = TestDto(urls = urls)

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Large list under max size should not produce violations")
    }

    @Test
    @DisplayName("Should be case insensitive for protocols in list")
    fun shouldBeCaseInsensitiveForProtocolsInList() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "HTTP://example.com",
                "HtTpS://test.com",
                "https://demo.org"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Case-insensitive protocols should not produce violations")
    }

    @Test
    @DisplayName("Should reject URL with spaces in list")
    fun shouldRejectUrlWithSpacesInList() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "https://valid.com",
                "http://example .com"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "URL with spaces should produce violations")
        assertTrue(
            violations.any { it.message.contains("index 1") },
            "Should report invalid URL at index 1"
        )
    }

    @Test
    @DisplayName("Should validate message format includes index")
    fun shouldValidateMessageFormatIncludesIndex() {
        // Given
        val dto = TestDto(
            urls = listOf(
                "https://valid.com",
                "https://also-valid.com",
                "not-a-valid-url"
            )
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "Should produce violations")
        val message = violations.first().message
        assertTrue(
            message.contains("index 2"),
            "Error message should mention correct index: $message"
        )
    }
}
