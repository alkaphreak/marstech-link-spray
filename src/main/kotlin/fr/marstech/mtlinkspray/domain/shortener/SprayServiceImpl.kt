package fr.marstech.mtlinkspray.domain.shortener
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.shared.QuotaPort
import org.springframework.stereotype.Service

@Service
class SprayServiceImpl(
    private val shortenerService: ShortenerService,
    private val writeQuota: QuotaPort,
) : SprayService {

    /**
     * Shortens every URL in the list, builds a spray link from the shortened URLs,
     * then shortens that spray link into a single short URL.
     */
    override fun shortenAndSpray(
        urls: List<String>,
        client: ClientContext,
        links: LinkUrls,
        onChildShortened: (String) -> Unit,
    ): String {
        // Same cap as the API's @ValidUrlList, which the form path does not go through
        if (urls.size > MAX_URLS) throw IllegalArgumentException("A spray holds at most $MAX_URLS URLs")
        // One token per short link created: each URL plus the spray link itself (MLS-161, ADR 0006)
        writeQuota.count(client.ipAddress ?: "unknown", urls.size + 1L)
        val shortenedUrls = urls.map { url ->
            (shortenerService.shorten(url, links)
                ?: throw UrlShorteningException("Failed to shorten URL", null))
                .also(onChildShortened)
        }
        return shortenerService.shorten(links.spray(shortenedUrls), links)
            ?: throw UrlShorteningException("Failed to shorten spray URL", null)
    }

    private companion object {
        const val MAX_URLS = 100
    }
}
