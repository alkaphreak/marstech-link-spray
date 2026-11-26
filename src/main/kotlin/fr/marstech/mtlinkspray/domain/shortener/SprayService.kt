package fr.marstech.mtlinkspray.domain.shortener
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.shared.QuotaPort
import org.springframework.stereotype.Service

@Service
class SprayService(
    private val shortenerService: ShortenerService,
    private val writeQuota: QuotaPort,
) {

    /**
     * Shortens each URL in the list, builds a spray link from the shortened URLs,
     * then shortens the resulting spray link into a single short URL.
     *
     * @param urls   the list of raw target URLs
     * @param client the caller, whose IP pays the write quota
     * @param links  builds the absolute short and spray URLs
     * @return a single shortened URL that redirects to a spray page of shortened links
     */
    fun shortenAndSpray(urls: List<String>, client: ClientContext, links: LinkUrls): String =
        shortenAndSpray(urls, client, links) {}

    /**
     * Same as [shortenAndSpray], calling [onChildShortened] with each per-URL short link, which is
     * shareable on its own (the web UI records a write signature for each, MLS-254).
     */
    fun shortenAndSpray(
        urls: List<String>,
        client: ClientContext,
        links: LinkUrls,
        onChildShortened: (String) -> Unit,
    ): String {
        // Same cap as the API's @ValidUrlList, which the form path does not go through
        require(urls.size <= MAX_URLS) { "A spray holds at most $MAX_URLS URLs" }
        // One token per short link created: each URL plus the spray link itself (MLS-161, ADR 0006)
        writeQuota.count(client.ipAddress ?: "unknown", urls.size + 1L)
        // All URLs are checked at once before any link is created: a rejected spray leaves no orphan links,
        // and the spray link only wraps the short links just created from them (MLS-120)
        shortenerService.rejectMalicious(urls)
        val shortenedUrls = urls.map { url ->
            (shortenerService.store(url, links)
                ?: throw UrlShorteningException("Failed to shorten URL", null))
                .also(onChildShortened)
        }
        return shortenerService.store(links.spray(shortenedUrls), links)
            ?: throw UrlShorteningException("Failed to shorten spray URL", null)
    }

    companion object {
        private const val MAX_URLS = 100

        /**
         * Parses a string into a list of non-blank, trimmed links.
         */
        @JvmStatic
        fun getLinkList(inputLinkList: String): List<String> =
            inputLinkList.lineSequence()
                .map(String::trim)
                .filter(String::isNotBlank)
                .toSet()
                .toList()

        /**
         * Converts a list of links to a newline-separated string, or null if empty/null.
         */
        @JvmStatic
        fun getLinkListText(linkList: List<String>?): String? =
            linkList?.takeIf { it.isNotEmpty() }?.joinToString("\n")
    }
}
