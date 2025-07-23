package fr.marstech.mtlinkspray.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShortenerServiceUnitTest {

    @Test
    void getShortenedLink() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getServerName()).thenReturn("localhost");
        when(mockRequest.getServerPort()).thenReturn(8080);
        when(mockRequest.getScheme()).thenReturn("http");
        String uid = "abc123";

        // Act
        String result = ShortenerService.getShortenedLink(mockRequest, uid);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(uid));
        assertTrue(result.startsWith("http://localhost:8080"));
    }

    @Test
    void getShortenedLink_shouldFilterDefaultPort() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getServerName()).thenReturn("my-server-name");
        when(mockRequest.getServerPort()).thenReturn(443);
        when(mockRequest.getScheme()).thenReturn("https");
        String uid = "def605";

        // Act
        String result = ShortenerService.getShortenedLink(mockRequest, uid);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(uid));
        assertTrue(result.startsWith("https://my-server-name/"));
    }
}