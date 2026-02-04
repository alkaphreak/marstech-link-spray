package fr.marstech.mtlinkspray.validation

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("ValidUrl Annotation Tests")
class ValidUrlValidatorTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    // Test data class for validation
    data class TestDto(
        @field:ValidUrl
        val url: String?
    )

    data class TestDtoWithCustomProtocols(
        @field:ValidUrl(protocols = ["ftp", "sftp"])
        val url: String?
    )

    data class TestDtoWithMaxLength(
        @field:ValidUrl(maxLength = 50)
        val url: String?
    )

    @Test
    @DisplayName("Should accept valid HTTP URL")
    fun shouldAcceptValidHttpUrl() {
        // Given
        val dto = TestDto(url = "http://example.com")

        // When
        val violations: Set<ConstraintViolation<TestDto>> = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Valid HTTP URL should not produce violations")
    }

    @Test
    @DisplayName("Should accept valid HTTPS URL")
    fun shouldAcceptValidHttpsUrl() {
        // Given
        val dto = TestDto(url = "https://example.com")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Valid HTTPS URL should not produce violations")
    }

    @Test
    @DisplayName("Should accept valid HTTPS URL with path")
    fun shouldAcceptValidHttpsUrlWithPath() {
        // Given
        val dto = TestDto(url = "https://example.com/path/to/resource")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Valid HTTPS URL with path should not produce violations")
    }

    @Test
    @DisplayName("Should accept valid HTTPS URL with query parameters")
    fun shouldAcceptValidHttpsUrlWithQueryParams() {
        // Given
        val dto = TestDto(url = "https://example.com/path?param1=value1&param2=value2")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Valid HTTPS URL with query params should not produce violations")
    }

    @Test
    @DisplayName("Should accept valid HTTPS URL with port")
    fun shouldAcceptValidHttpsUrlWithPort() {
        // Given
        val dto = TestDto(url = "https://example.com:8080/path")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Valid HTTPS URL with port should not produce violations")
    }

    @Test
    @DisplayName("Should accept null URL (validation handled by @NotNull)")
    fun shouldAcceptNullUrl() {
        // Given
        val dto = TestDto(url = null)

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Null URL should be handled by @NotNull, not @ValidUrl")
    }

    @Test
    @DisplayName("Should accept blank URL (validation handled by @NotBlank)")
    fun shouldAcceptBlankUrl() {
        // Given
        val dto = TestDto(url = "   ")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "Blank URL should be handled by @NotBlank, not @ValidUrl")
    }

    @Test
    @DisplayName("Should reject URL without protocol")
    fun shouldRejectUrlWithoutProtocol() {
        // Given
        val dto = TestDto(url = "example.com")

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "URL without protocol should produce violations")
        assertTrue(
            violations.any { it.message.contains("protocol") },
            "Error message should mention protocol requirement"
        )
    }

    @Test
    @DisplayName("Should reject URL with invalid protocol")
    fun shouldRejectUrlWithInvalidProtocol() {
        // Given
        val dto = TestDto(url = "ftp://example.com")

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "URL with invalid protocol should produce violations")
        assertTrue(
            violations.any { it.message.contains("protocol must be one of") },
            "Error message should mention allowed protocols"
        )
    }

    @Test
    @DisplayName("Should accept URL with custom allowed protocols")
    fun shouldAcceptUrlWithCustomAllowedProtocols() {
        // Given
        val dto = TestDtoWithCustomProtocols(url = "ftp://example.com")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "URL with custom allowed protocol should not produce violations")
    }

    @Test
    @DisplayName("Should reject URL with protocol not in custom list")
    fun shouldRejectUrlWithProtocolNotInCustomList() {
        // Given
        val dto = TestDtoWithCustomProtocols(url = "http://example.com")

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "URL with protocol not in custom list should produce violations")
    }

    @Test
    @DisplayName("Should reject URL exceeding max length")
    fun shouldRejectUrlExceedingMaxLength() {
        // Given
        val longUrl = "http://example.com/" + "a".repeat(100)
        val dto = TestDtoWithMaxLength(url = longUrl)

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "URL exceeding max length should produce violations")
        assertTrue(
            violations.any { it.message.contains("length cannot exceed") },
            "Error message should mention length limit"
        )
    }

    @Test
    @DisplayName("Should accept URL at max length boundary")
    fun shouldAcceptUrlAtMaxLengthBoundary() {
        // Given
        val url = "http://example.com/" + "a".repeat(28) // Total = 50 chars
        val dto = TestDtoWithMaxLength(url = url)

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "URL at max length boundary should not produce violations")
    }

    @Test
    @DisplayName("Should reject malformed URL")
    fun shouldRejectMalformedUrl() {
        // Given
        val dto = TestDto(url = "http://[invalid")

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "Malformed URL should produce violations")
        assertTrue(
            violations.any { it.message.contains("Invalid URL format") },
            "Error message should mention invalid format"
        )
    }

    @Test
    @DisplayName("Should reject URL with spaces")
    fun shouldRejectUrlWithSpaces() {
        // Given
        val dto = TestDto(url = "http://example .com")

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "URL with spaces should produce violations")
    }

    @Test
    @DisplayName("Should accept URL with special characters in path")
    fun shouldAcceptUrlWithSpecialCharactersInPath() {
        // Given
        val dto = TestDto(url = "https://example.com/path-with_special.chars~123")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "URL with special chars in path should not produce violations")
    }

    @Test
    @DisplayName("Should accept URL with fragment")
    fun shouldAcceptUrlWithFragment() {
        // Given
        val dto = TestDto(url = "https://example.com/path#section")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "URL with fragment should not produce violations")
    }

    @Test
    @DisplayName("Should accept URL with authentication")
    fun shouldAcceptUrlWithAuthentication() {
        // Given
        val dto = TestDto(url = "https://user:pass@example.com/path")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "URL with authentication should not produce violations")
    }

    @Test
    @DisplayName("Should accept URL with IP address")
    fun shouldAcceptUrlWithIpAddress() {
        // Given
        val dto = TestDto(url = "http://192.168.1.1:8080/path")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "URL with IP address should not produce violations")
    }

    @Test
    @DisplayName("Should accept URL with subdomain")
    fun shouldAcceptUrlWithSubdomain() {
        // Given
        val dto = TestDto(url = "https://sub.domain.example.com")

        // When
        val violations = validator.validate(dto)

        // Then
        assertTrue(violations.isEmpty(), "URL with subdomain should not produce violations")
    }

    @Test
    @DisplayName("Should be case insensitive for protocols")
    fun shouldBeCaseInsensitiveForProtocols() {
        // Given
        val dtoUpperCase = TestDto(url = "HTTP://example.com")
        val dtoMixedCase = TestDto(url = "HtTpS://example.com")

        // When
        val violationsUpperCase = validator.validate(dtoUpperCase)
        val violationsMixedCase = validator.validate(dtoMixedCase)

        // Then
        assertTrue(violationsUpperCase.isEmpty(), "Uppercase protocol should be accepted")
        assertTrue(violationsMixedCase.isEmpty(), "Mixed-case protocol should be accepted")
    }

    @Test
    @DisplayName("Should reject URL with empty host")
    fun shouldRejectUrlWithEmptyHost() {
        // Given - URL with protocol but no host
        val dto = TestDto(url = "http://")

        // When
        val violations = validator.validate(dto)

        // Then
        assertFalse(violations.isEmpty(), "URL with empty host should produce violations")
    }

    @Test
    @DisplayName("Should validate multiple violations correctly")
    fun shouldValidateMultipleViolationsCorrectly() {
        // Given
        data class MultiFieldDto(
            @field:ValidUrl val url1: String?,
            @field:ValidUrl val url2: String?,
            @field:ValidUrl val url3: String?
        )

        val dto = MultiFieldDto(
            url1 = "http://valid.com",
            url2 = "invalid-url",
            url3 = "ftp://invalid-protocol.com"
        )

        // When
        val violations = validator.validate(dto)

        // Then
        assertEquals(2, violations.size, "Should have exactly 2 violations")
    }

    @Test
    @DisplayName("Validator initialization should not throw")
    fun validatorInitializationShouldNotThrow() {
        // Given & When & Then
        assertDoesNotThrow {
            val testValidator = ValidUrlValidator()
            val annotation = TestDto::class.java
                .getDeclaredField("url")
                .getAnnotation(ValidUrl::class.java)
            testValidator.initialize(annotation)
        }
    }
}
