package fr.marstech.mtlinkspray.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Enumeration;

import static java.util.Collections.enumeration;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NetworkUtilsUnitTest {

  private HttpServletRequest mockRequest;

  @BeforeEach
  void setUp() {
    this.mockRequest = mock(HttpServletRequest.class);
  }

  @Test
  void getHeadersAsMap() {
    Enumeration<String> headerNames = enumeration(of("X-Test-Header", "Another-Header"));
    when(mockRequest.getHeaderNames()).thenReturn(headerNames);
    when(mockRequest.getHeader("X-Test-Header")).thenReturn("test-value");
    when(mockRequest.getHeader("Another-Header")).thenReturn("another-value");

    var result = NetworkUtils.getHeadersAsMap(mockRequest);
    assertEquals(2, result.size());
    assertEquals("test-value", result.get("X-Test-Header"));
    assertEquals("another-value", result.get("Another-Header"));
  }

  @Test
  void getHeadersAsMap_nullHeaderNamesShouldNotFail() {
    when(mockRequest.getHeaderNames()).thenReturn(null);
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
    when(mockRequest.getHeaderNames()).thenReturn(enumeration(of()));
    when(mockRequest.getServerPort()).thenReturn(8080);
    assertEquals("8080", NetworkUtils.getPort(mockRequest));
  }

  @Test
  void getScheme() {
    when(mockRequest.getHeaderNames()).thenReturn(enumeration(of()));
    when(mockRequest.getScheme()).thenReturn("https");
    assertEquals("https", NetworkUtils.getScheme(mockRequest));
  }

  @Test
  void getHost() {
    when(mockRequest.getHeaderNames()).thenReturn(enumeration(of()));
    when(mockRequest.getServerName()).thenReturn("localhost");
    assertEquals("localhost", NetworkUtils.getHost(mockRequest));
  }

  @Test
  void isValidUrl() {
    assertTrue(NetworkUtils.isValidUrl("https://www.example.com"));
    assertFalse(NetworkUtils.isValidUrl("not-a-url"));
    assertFalse(NetworkUtils.isValidUrl(""));
  }
}
