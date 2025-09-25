package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.utils.NetworkUtils.getFilteredPort
import fr.marstech.mtlinkspray.utils.NetworkUtils.getHost
import fr.marstech.mtlinkspray.utils.NetworkUtils.getScheme
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.util.UriComponentsBuilder

interface ShortenerService {

    /**
     * Get the shortened UID from the target URL
     *
     * @param url                the target URL
     * @param httpServletRequest the request
     * @return the shortened UID
     */
    fun shorten(url: String, httpServletRequest: HttpServletRequest): String

    /**
     * Get the target URL from the shortened UID
     *
     * @param uid                the shortened UID
     * @param httpServletRequest the request
     * @return the target URL
     */
    fun getTarget(uid: String, httpServletRequest: HttpServletRequest): String

    companion object {
        /**
         * Get the target URL from the shortened UID
         *
         * @param uid                the shortened UID
         * @param httpServletRequest the request
         * @return the target URL
         */
        @JvmStatic
        fun getShortenedLink(httpServletRequest: HttpServletRequest, uid: String): String =
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