package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.exception.UrlShorteningException
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

internal class SprayServiceImplTest {

    private val shortenerService: ShortenerService = mock(ShortenerService::class.java)
    private val sprayService: SprayService = SprayServiceImpl(shortenerService)
    private lateinit var httpServletRequest: HttpServletRequest

    @BeforeEach
    fun setUp() {
        httpServletRequest = mock(HttpServletRequest::class.java)
        `when`(httpServletRequest.serverName).thenReturn("localhost")
        `when`(httpServletRequest.serverPort).thenReturn(80)
        `when`(httpServletRequest.scheme).thenReturn("http")
    }

    @Test
    fun shortenAndSprayShouldReturnShortenedSprayUrl() {
        // Given
        val urls = listOf("https://example.com", "https://example.org")
        val shortUrl1 = "http://localhost/abc"
        val shortUrl2 = "http://localhost/def"
        val finalShortUrl = "http://localhost/xyz"

        `when`(shortenerService.shorten("https://example.com", httpServletRequest)).thenReturn(shortUrl1)
        `when`(shortenerService.shorten("https://example.org", httpServletRequest)).thenReturn(shortUrl2)
        val sprayUrl = SprayService.getLinkSpray(httpServletRequest, listOf(shortUrl1, shortUrl2))
        `when`(shortenerService.shorten(sprayUrl, httpServletRequest)).thenReturn(finalShortUrl)

        // When
        val result = sprayService.shortenAndSpray(urls, httpServletRequest)

        // Then
        assertEquals(finalShortUrl, result)
    }

    @Test
    fun shortenAndSprayShouldThrowExceptionWhenUrlShorteningFails() {
        // Given
        val urls = listOf("https://example.com")
        `when`(shortenerService.shorten("https://example.com", httpServletRequest)).thenReturn(null)

        // When / Then
        assertThrows(UrlShorteningException::class.java) {
            sprayService.shortenAndSpray(urls, httpServletRequest)
        }
    }

    @Test
    fun shortenAndSprayShouldThrowExceptionWhenSprayUrlShorteningFails() {
        // Given
        val urls = listOf("https://example.com")
        val shortUrl = "http://localhost/abc"
        `when`(shortenerService.shorten("https://example.com", httpServletRequest)).thenReturn(shortUrl)
        val sprayUrl = SprayService.getLinkSpray(httpServletRequest, listOf(shortUrl))
        `when`(shortenerService.shorten(sprayUrl, httpServletRequest)).thenReturn(null)

        // When / Then
        assertThrows(UrlShorteningException::class.java) {
            sprayService.shortenAndSpray(urls, httpServletRequest)
        }
    }

    @Test
    fun shortenAndSprayShouldHandleSingleUrl() {
        // Given
        val urls = listOf("https://single.example.com")
        val shortUrl = "http://localhost/s1"
        val finalShortUrl = "http://localhost/final"
        `when`(shortenerService.shorten("https://single.example.com", httpServletRequest)).thenReturn(shortUrl)
        val sprayUrl = SprayService.getLinkSpray(httpServletRequest, listOf(shortUrl))
        `when`(shortenerService.shorten(sprayUrl, httpServletRequest)).thenReturn(finalShortUrl)

        // When
        val result = sprayService.shortenAndSpray(urls, httpServletRequest)

        // Then
        assertEquals(finalShortUrl, result)
    }
}
