package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.utils.NetworkUtils.filterDefaultPort
import fr.marstech.mtlinkspray.utils.NetworkUtils.getHost
import fr.marstech.mtlinkspray.utils.NetworkUtils.getPort
import fr.marstech.mtlinkspray.utils.NetworkUtils.getScheme
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.util.UriComponentsBuilder

interface SprayService {

    /**
     * Shortens each URL in the list, builds a spray link from the shortened URLs,
     * then shortens the resulting spray link into a single short URL.
     *
     * @param urls               the list of raw target URLs
     * @param httpServletRequest the current HTTP request (used to build absolute URLs)
     * @return a single shortened URL that redirects to a spray page of shortened links
     */
    fun shortenAndSpray(urls: List<String>, httpServletRequest: HttpServletRequest): String

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

        /**
         * Builds a spray page URL with the provided links or returns null if the list is empty/null.
         */
        @JvmStatic
        fun getLinkSpray(
            httpServletRequest: HttpServletRequest,
            linkList: List<String>
        ): String = UriComponentsBuilder.newInstance()
            .scheme(getScheme(httpServletRequest))
            .host(getHost(httpServletRequest))
            .port(filterDefaultPort(getPort(httpServletRequest)))
            .path("/spray/open")
            .queryParam("spray", *linkList.toTypedArray())
            .build()
            .encode()
            .toString()
    }
}