package fr.marstech.mtlinkspray.domain.shortener
import fr.marstech.mtlinkspray.adapter.inbound.web.shortener.RequestLinkUrls
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.adapter.outbound.ratelimit.AnonymousQuota
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

internal class SprayServiceImplTest {

    private val shortenerService: ShortenerService = mock(ShortenerService::class.java)
    private val sprayService: SprayService = SprayServiceImpl(shortenerService, AnonymousQuota("Write", 1, 0))
    private lateinit var httpServletRequest: HttpServletRequest
    private lateinit var links: RequestLinkUrls
    private val client = ClientContext(ipAddress = null)

    @BeforeEach
    fun setUp() {
        httpServletRequest = mock(HttpServletRequest::class.java)
        `when`(httpServletRequest.serverName).thenReturn("localhost")
        `when`(httpServletRequest.serverPort).thenReturn(80)
        `when`(httpServletRequest.scheme).thenReturn("http")
        links = RequestLinkUrls(httpServletRequest)
    }

    @Test
    fun shortenAndSprayShouldReturnShortenedSprayUrl() {
        // Given
        val urls = listOf("https://example.com", "https://example.org")
        val shortUrl1 = "http://localhost/abc"
        val shortUrl2 = "http://localhost/def"
        val finalShortUrl = "http://localhost/xyz"

        `when`(shortenerService.shorten("https://example.com", links)).thenReturn(shortUrl1)
        `when`(shortenerService.shorten("https://example.org", links)).thenReturn(shortUrl2)
        val sprayUrl = links.spray(listOf(shortUrl1, shortUrl2))
        `when`(shortenerService.shorten(sprayUrl, links)).thenReturn(finalShortUrl)

        // When
        val children = mutableListOf<String>()
        val result = sprayService.shortenAndSpray(urls, client, links) { children += it }

        // Then: only the per-URL links reach the callback, not the final spray link
        assertEquals(finalShortUrl, result)
        assertEquals(listOf(shortUrl1, shortUrl2), children)
    }

    @Test
    fun shortenAndSprayShouldThrowExceptionWhenUrlShorteningFails() {
        // Given
        val urls = listOf("https://example.com")
        `when`(shortenerService.shorten("https://example.com", links)).thenReturn(null)

        // When / Then
        assertThrows(UrlShorteningException::class.java) {
            sprayService.shortenAndSpray(urls, client, links)
        }
    }

    @Test
    fun shortenAndSprayShouldThrowExceptionWhenSprayUrlShorteningFails() {
        // Given
        val urls = listOf("https://example.com")
        val shortUrl = "http://localhost/abc"
        `when`(shortenerService.shorten("https://example.com", links)).thenReturn(shortUrl)
        val sprayUrl = links.spray(listOf(shortUrl))
        `when`(shortenerService.shorten(sprayUrl, links)).thenReturn(null)

        // When / Then
        assertThrows(UrlShorteningException::class.java) {
            sprayService.shortenAndSpray(urls, client, links)
        }
    }

    @Test
    fun shortenAndSprayShouldHandleSingleUrl() {
        // Given
        val urls = listOf("https://single.example.com")
        val shortUrl = "http://localhost/s1"
        val finalShortUrl = "http://localhost/final"
        `when`(shortenerService.shorten("https://single.example.com", links)).thenReturn(shortUrl)
        val sprayUrl = links.spray(listOf(shortUrl))
        `when`(shortenerService.shorten(sprayUrl, links)).thenReturn(finalShortUrl)

        // When
        val result = sprayService.shortenAndSpray(urls, client, links)

        // Then
        assertEquals(finalShortUrl, result)
    }

    @Test
    fun shortenAndSprayShouldSpendOneTokenPerLinkBeforeShortening() {
        // Given: 2 URLs cost 3 tokens, and this IP already spent 1 of its 3
        val quota = AnonymousQuota("Write", 3, 600_000)
        quota.count("unknown")
        val limited = SprayServiceImpl(shortenerService, quota)

        // When / Then
        assertThrows(AnonymousQuota.QuotaExceededException::class.java) {
            limited.shortenAndSpray(listOf("https://example.com", "https://example.org"), client, links)
        }
        org.mockito.Mockito.verifyNoInteractions(shortenerService)
    }

    @Test
    fun shortenAndSprayShouldRejectMoreThanOneHundredUrls() {
        // When / Then
        val e = assertThrows(org.springframework.web.server.ResponseStatusException::class.java) {
            sprayService.shortenAndSpray(List(101) { "https://example.com/$it" }, client, links)
        }
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, e.statusCode)
        org.mockito.Mockito.verifyNoInteractions(shortenerService)
    }
}
