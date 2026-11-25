package fr.marstech.mtlinkspray.adapter.inbound.web.shortener
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

internal class RequestLinkUrlsTest {

    @Test
    fun getShortenedLink() {
        // Arrange
        val mockRequest = mock(HttpServletRequest::class.java)
        `when`(mockRequest.serverName).thenReturn("localhost")
        `when`(mockRequest.serverPort).thenReturn(8080)
        `when`(mockRequest.scheme).thenReturn("http")
        val uid = "abc123"

        // Act
        val result = RequestLinkUrls(mockRequest).shortLink(uid)

        // Assert
        Assertions.assertNotNull(result)
        Assertions.assertTrue(result.contains(uid))
        Assertions.assertTrue(result.startsWith("http://localhost:8080"))
    }

    @Test
    fun getShortenedLink_shouldFilterDefaultPort() {
        // Arrange
        val mockRequest = mock(HttpServletRequest::class.java)
        `when`(mockRequest.serverName).thenReturn("my-server-name")
        `when`(mockRequest.serverPort).thenReturn(443)
        `when`(mockRequest.scheme).thenReturn("https")
        val uid = "def605"

        // Act
        val result = RequestLinkUrls(mockRequest).shortLink(uid)

        // Assert
        Assertions.assertNotNull(result)
        Assertions.assertTrue(result.contains(uid))
        Assertions.assertTrue(result.startsWith("https://my-server-name/"))
    }

    @Test
    fun getShortenedLink_withBlankUid_returnsNull() {
        val mockRequest = mock(HttpServletRequest::class.java)
        `when`(mockRequest.serverName).thenReturn("localhost")
        `when`(mockRequest.serverPort).thenReturn(8080)
        `when`(mockRequest.scheme).thenReturn("http")
        RequestLinkUrls(mockRequest).shortLink("   ")
        // TODO test exception
    }

    @Test
    fun getShortenedLink_withSpecialCharacters_encodesUid() {
        val mockRequest = mock(HttpServletRequest::class.java)
        `when`(mockRequest.serverName).thenReturn("localhost")
        `when`(mockRequest.serverPort).thenReturn(8080)
        `when`(mockRequest.scheme).thenReturn("http")
        val uid = "abc 123"
        val result = RequestLinkUrls(mockRequest).shortLink(uid)
        Assertions.assertTrue(result.contains("abc%20123"))
    }
}
