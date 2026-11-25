package fr.marstech.mtlinkspray.adapter.inbound.web.shortener
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.filterDefaultPort
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.getFilteredPort
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.getHost
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.getPort
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.getScheme
import fr.marstech.mtlinkspray.domain.shortener.LinkUrls
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.util.UriComponentsBuilder

/** Short and spray URLs on the scheme, host and port the request came through (forwarded headers first). */
class RequestLinkUrls(private val request: HttpServletRequest) : LinkUrls {

    override fun shortLink(id: String): String = UriComponentsBuilder.newInstance()
        .scheme(getScheme(request))
        .host(getHost(request))
        .port(getFilteredPort(request))
        .path(id)
        .build()
        .encode()
        .toString()

    override fun spray(links: List<String>): String = UriComponentsBuilder.newInstance()
        .scheme(getScheme(request))
        .host(getHost(request))
        .port(filterDefaultPort(getPort(request)))
        .path("/spray/open")
        .queryParam("spray", *links.toTypedArray())
        .build()
        .encode()
        .toString()
}
