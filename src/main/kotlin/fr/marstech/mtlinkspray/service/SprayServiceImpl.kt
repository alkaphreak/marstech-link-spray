package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.exception.UrlShorteningException
import fr.marstech.mtlinkspray.service.SprayService.Companion.getLinkSpray
import fr.marstech.mtlinkspray.utils.NetworkUtils
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class SprayServiceImpl(
    private val shortenerService: ShortenerService,
    private val writeQuota: AnonymousQuota,
) : SprayService {

    /**
     * Shortens every URL in the list, builds a spray link from the shortened URLs,
     * then shortens that spray link into a single short URL.
     */
    override fun shortenAndSpray(
        urls: List<String>,
        httpServletRequest: HttpServletRequest,
        onChildShortened: (String) -> Unit,
    ): String {
        // Same cap as the API's @ValidUrlList, which the form path does not go through
        if (urls.size > MAX_URLS) throw ResponseStatusException(BAD_REQUEST, "A spray holds at most $MAX_URLS URLs")
        // One token per short link created: each URL plus the spray link itself (MLS-161, ADR 0006)
        writeQuota.count(NetworkUtils.getIpAddress(httpServletRequest) ?: "unknown", urls.size + 1L)
        val shortenedUrls = urls.map { url ->
            (shortenerService.shorten(url, httpServletRequest)
                ?: throw UrlShorteningException("Failed to shorten URL", null))
                .also(onChildShortened)
        }
        val sprayUrl = getLinkSpray(httpServletRequest, shortenedUrls)
        return shortenerService.shorten(sprayUrl, httpServletRequest)
            ?: throw UrlShorteningException("Failed to shorten spray URL", null)
    }

    private companion object {
        const val MAX_URLS = 100
    }
}
