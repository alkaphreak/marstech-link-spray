package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.service.ShortenerService.Companion.getShortenedLink
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

internal class ShortenerServiceUnitTest {

    @Test
    fun getShortenedLink() {
        // Arrange
        val mockRequest = mock(HttpServletRequest::class.java)
        Mockito.`when`(mockRequest.serverName).thenReturn("localhost")
        Mockito.`when`(mockRequest.serverPort).thenReturn(8080)
        Mockito.`when`(mockRequest.scheme).thenReturn("http")
        val uid = "abc123"

        // Act
        val result = getShortenedLink(mockRequest, uid)

        // Assert
        Assertions.assertNotNull(result)
        Assertions.assertTrue(result.contains(uid))
        Assertions.assertTrue(result.startsWith("http://localhost:8080"))
    }

    @Test
    fun getShortenedLink_shouldFilterDefaultPort() {
        // Arrange
        val mockRequest = mock(HttpServletRequest::class.java)
        Mockito.`when`(mockRequest.serverName).thenReturn("my-server-name")
        Mockito.`when`(mockRequest.serverPort).thenReturn(443)
        Mockito.`when`(mockRequest.scheme).thenReturn("https")
        val uid = "def605"

        // Act
        val result = getShortenedLink(mockRequest, uid)

        // Assert
        Assertions.assertNotNull(result)
        Assertions.assertTrue(result.contains(uid))
        Assertions.assertTrue(result.startsWith("https://my-server-name/"))
    }

    @Test
    fun getShortenedLink_withBlankUid_returnsNull() {
        val mockRequest = mock(HttpServletRequest::class.java)
        Mockito.`when`(mockRequest.serverName).thenReturn("localhost")
        Mockito.`when`(mockRequest.serverPort).thenReturn(8080)
        Mockito.`when`(mockRequest.scheme).thenReturn("http")
        getShortenedLink(mockRequest, "   ")
        // TODO test exception
    }

    @Test
    fun getShortenedLink_withSpecialCharacters_encodesUid() {
        val mockRequest = mock(HttpServletRequest::class.java)
        Mockito.`when`(mockRequest.serverName).thenReturn("localhost")
        Mockito.`when`(mockRequest.serverPort).thenReturn(8080)
        Mockito.`when`(mockRequest.scheme).thenReturn("http")
        val uid = "abc 123"
        val result = getShortenedLink(mockRequest, uid)
        Assertions.assertTrue(result.contains("abc%20123"))
    }
}
