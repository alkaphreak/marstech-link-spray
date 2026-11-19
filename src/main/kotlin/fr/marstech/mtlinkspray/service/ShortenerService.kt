package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.LinkStatsResponse
import fr.marstech.mtlinkspray.utils.NetworkUtils.getFilteredPort
import fr.marstech.mtlinkspray.utils.NetworkUtils.getHost
import fr.marstech.mtlinkspray.utils.NetworkUtils.getScheme
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import org.springframework.web.util.UriComponentsBuilder

interface ShortenerService {

    /**
     * Get the shortened UID from the target URL
     *
     * @param url                the target URL
     * @param httpServletRequest the request
     * @return the shortened UID
     */
    fun shorten(url: String, httpServletRequest: HttpServletRequest): String?

    /**
     * Get the target URL from the shortened UID and count the click, as a robot's when the User-Agent says so
     *
     * @param uid       the shortened UID
     * @param userAgent the request's User-Agent, read in memory and never stored (MLS-273)
     * @return the target URL
     */
    fun getTarget(uid: String, userAgent: String?): String

    /** Click counts (people, robots) and last click date of a short link (MLS-261, MLS-273). */
    fun getStats(uid: String): LinkStatsResponse

    companion object {
        /**
         * Get the target URL from the shortened UID
         *
         * @param uid                the shortened UID
         * @param httpServletRequest the request
         * @return the target URL
         */
        @JvmStatic
        fun getShortenedLink(
            @NotBlank uid: String,
            httpServletRequest: HttpServletRequest
        ): String =
            UriComponentsBuilder.newInstance()
                .scheme(getScheme(httpServletRequest))
                .host(getHost(httpServletRequest))
                .port(getFilteredPort(httpServletRequest))
                .path(uid)
                .build()
                .encode()
                .toString()
    }
}