package fr.marstech.mtlinkspray.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Enumeration;

import static java.util.Collections.enumeration;
import static org.junit.jupiter.api.Assertions.*;

class NetworkUtilsUnitTest {

    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        this.mockRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
    }

    @Test
    void getHeadersAsMap() {
        Enumeration<String> headerNames = enumeration(java.util.List.of("X-Test-Header", "Another-Header"));
        org.mockito.Mockito.when(mockRequest.getHeaderNames()).thenReturn(headerNames);
        org.mockito.Mockito.when(mockRequest.getHeader("X-Test-Header")).thenReturn("test-value");
        org.mockito.Mockito.when(mockRequest.getHeader("Another-Header")).thenReturn("another-value");

        var result = NetworkUtils.getHeadersAsMap(mockRequest);
        assertEquals(2, result.size());
        assertEquals("test-value", result.get("X-Test-Header"));
        assertEquals("another-value", result.get("Another-Header"));
    }

    @Test
    void getHeadersAsMap_nullHeaderNamesShouldNotFail() {
        org.mockito.Mockito.when(mockRequest.getHeaderNames()).thenReturn(null);
        var result = NetworkUtils.getHeadersAsMap(mockRequest);
        assertEquals(0, result.size());
    }

    @Test
    void filterDefaultPort() {
        assertNull(NetworkUtils.filterDefaultPort(""));
        assertNull(NetworkUtils.filterDefaultPort(" "));
        assertNull(NetworkUtils.filterDefaultPort(null));
        assertNull(NetworkUtils.filterDefaultPort("80"));
        assertNull(NetworkUtils.filterDefaultPort("443"));
        assertEquals("8080", NetworkUtils.filterDefaultPort("8080"));
    }

    @Test
    void getPort() {
        org.mockito.Mockito.when(mockRequest.getHeaderNames()).thenReturn(enumeration(java.util.List.of()));
        org.mockito.Mockito.when(mockRequest.getServerPort()).thenReturn(8080);
        assertEquals("8080", NetworkUtils.getPort(mockRequest));
    }

    @Test
    void getScheme() {
        org.mockito.Mockito.when(mockRequest.getHeaderNames()).thenReturn(enumeration(java.util.List.of()));
        org.mockito.Mockito.when(mockRequest.getScheme()).thenReturn("https");
        assertEquals("https", NetworkUtils.getScheme(mockRequest));
    }

    @Test
    void getHost() {
        org.mockito.Mockito.when(mockRequest.getHeaderNames()).thenReturn(enumeration(java.util.List.of()));
        org.mockito.Mockito.when(mockRequest.getServerName()).thenReturn("localhost");
        assertEquals("localhost", NetworkUtils.getHost(mockRequest));
    }

    @Test
    void isValidUrl() {
        assertTrue(NetworkUtils.isValidUrl("https://www.example.com"));
        assertFalse(NetworkUtils.isValidUrl("not-a-url"));
        assertFalse(NetworkUtils.isValidUrl(""));
    }
}