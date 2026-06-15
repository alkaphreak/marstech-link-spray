package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.exception.UrlShorteningException
import fr.marstech.mtlinkspray.service.SprayService.Companion.getLinkSpray
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

@Service
class SprayServiceImpl(
    private val shortenerService: ShortenerService
) : SprayService {

    /**
     * Shortens every URL in the list, builds a spray link from the shortened URLs,
     * then shortens that spray link into a single short URL.
     */
    override fun shortenAndSpray(urls: List<String>, httpServletRequest: HttpServletRequest): String {
        val shortenedUrls = urls.map { url ->
            shortenerService.shorten(url, httpServletRequest)
                ?: throw UrlShorteningException("Failed to shorten URL", null)
        }
        val sprayUrl = getLinkSpray(httpServletRequest, shortenedUrls)
        return shortenerService.shorten(sprayUrl, httpServletRequest)
            ?: throw UrlShorteningException("Failed to shorten spray URL", null)
    }
}