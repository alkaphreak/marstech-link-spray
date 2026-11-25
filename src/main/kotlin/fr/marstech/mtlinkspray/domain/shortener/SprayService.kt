package fr.marstech.mtlinkspray.domain.shortener
import fr.marstech.mtlinkspray.domain.shared.ClientContext

interface SprayService {

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
    ): String

    companion object {

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
