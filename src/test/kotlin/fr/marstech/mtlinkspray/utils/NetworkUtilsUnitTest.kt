package fr.marstech.mtlinkspray.utils

import fr.marstech.mtlinkspray.utils.NetworkUtils.filterDefaultPort
import fr.marstech.mtlinkspray.utils.NetworkUtils.isValidUrl
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.*

internal class NetworkUtilsUnitTest {
    private var mockRequest: HttpServletRequest? = null

    @BeforeEach
    fun setUp() {
        this.mockRequest = mock(HttpServletRequest::class.java)
    }

    @Test
    fun getHeadersAsMap() {
        val headerNames = Collections.enumeration<String?>(mutableListOf<String?>("X-Test-Header", "Another-Header"))
        `when`(mockRequest!!.headerNames).thenReturn(headerNames)
        `when`(mockRequest!!.getHeader("X-Test-Header")).thenReturn("test-value")
        `when`(mockRequest!!.getHeader("Another-Header")).thenReturn("another-value")

        val result: Map<String, String> = NetworkUtils.getHeadersAsMap(mockRequest!!)
        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals("test-value", result["X-Test-Header"])
        Assertions.assertEquals("another-value", result["Another-Header"])
    }

    @Test
    fun getHeadersAsMap_nullHeaderNamesShouldNotFail() {
        `when`(mockRequest!!.headerNames).thenReturn(null)
        val result: Map<String, String> = NetworkUtils.getHeadersAsMap(mockRequest!!)
        Assertions.assertEquals(0, result.size)
    }

    @Test
    fun filterDefaultPort() {
        Assertions.assertNull(filterDefaultPort(""))
        Assertions.assertNull(filterDefaultPort(" "))
        Assertions.assertNull(filterDefaultPort(null))
        Assertions.assertNull(filterDefaultPort("80"))
        Assertions.assertNull(filterDefaultPort("443"))
        Assertions.assertEquals("8080", filterDefaultPort("8080"))
    }

    @Test
    fun getPort() {
        `when`(mockRequest!!.headerNames).thenReturn(
            Collections.enumeration(
                mutableListOf<String?>()
            )
        )
        `when`(mockRequest!!.serverPort).thenReturn(8080)
        Assertions.assertEquals("8080", NetworkUtils.getPort(mockRequest!!))
    }

    @Test
    fun getScheme() {
        `when`(mockRequest!!.headerNames).thenReturn(
            Collections.enumeration(
                mutableListOf<String?>()
            )
        )
        `when`(mockRequest!!.scheme).thenReturn("https")
        Assertions.assertEquals("https", NetworkUtils.getScheme(mockRequest!!))
    }

    @Test
    fun getHost() {
        `when`(mockRequest!!.headerNames).thenReturn(
            Collections.enumeration(
                mutableListOf<String?>()
            )
        )
        `when`(mockRequest!!.serverName).thenReturn("localhost")
        Assertions.assertEquals("localhost", NetworkUtils.getHost(mockRequest!!))
    }

    @Test
    fun isValidUrl() {
        Assertions.assertTrue(isValidUrl("https://www.example.com"))
        Assertions.assertFalse(isValidUrl("not-a-url"))
        Assertions.assertFalse(isValidUrl(""))
    }
}
