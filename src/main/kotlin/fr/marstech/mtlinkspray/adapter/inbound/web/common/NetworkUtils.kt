package fr.marstech.mtlinkspray.adapter.inbound.web.common
import jakarta.servlet.http.HttpServletRequest
import java.net.URI

object NetworkUtils {

    @JvmStatic
    fun getHeadersAsMap(httpServletRequest: HttpServletRequest): Map<String, String> =
        when (httpServletRequest.headerNames) {
            null -> emptyMap()
            else -> httpServletRequest.headerNames.toList().toSet().associateWith(httpServletRequest::getHeader)
        }

    @JvmStatic
    fun filterDefaultPort(port: String?): String? = when {
        port.isNullOrBlank() -> null
        HttpDefaultPortEnum.entries.any { it.port == port } -> null
        else -> port
    }

    @JvmStatic
    fun getPort(httpServletRequest: HttpServletRequest): String =
        getPort(httpServletRequest, getHeadersAsMap(httpServletRequest))

    private fun getPort(httpServletRequest: HttpServletRequest, headers: Map<String, String>): String =
        headers.getOrDefault(
            "x-forwarded-port", headers.getOrDefault("port", httpServletRequest.serverPort.toString())
        )

    @JvmStatic
    fun getFilteredPort(httpServletRequest: HttpServletRequest): String? = filterDefaultPort(
        getPort(httpServletRequest, getHeadersAsMap(httpServletRequest))
    )

    @JvmStatic
    fun getScheme(httpServletRequest: HttpServletRequest): String =
        getScheme(httpServletRequest, getHeadersAsMap(httpServletRequest))

    private fun getScheme(httpServletRequest: HttpServletRequest, headers: Map<String, String>): String =
        headers.getOrDefault("x-forwarded-proto", httpServletRequest.scheme)

    @JvmStatic
    fun getHost(httpServletRequest: HttpServletRequest): String =
        getHost(httpServletRequest, getHeadersAsMap(httpServletRequest))

    private fun getHost(
        httpServletRequest: HttpServletRequest, headers: Map<String, String>
    ): String = headers.getOrDefault(
        "x-forwarded-server", headers.getOrDefault(
            "x-forwarded-host", headers.getOrDefault("host", httpServletRequest.serverName)
        )
    ).split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().firstOrNull() ?: ""

    @JvmStatic
    fun isValidUrl(url: String): Boolean = try {
        URI(url).toURL().toString().isNotBlank()
    } catch (_: Exception) {
        false
    }

    /** An IPv4 or IPv6 literal: matching a host name instead would let parsing trigger a DNS lookup. */
    const val IP_LITERAL =
        "^((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)$|^[0-9A-Fa-f.]*:[0-9A-Fa-f:.]*$"

    /**
     * App Platform sets do-connecting-ip to the client address and keeps a client-sent X-Forwarded-For
     * as is (verified 2026-11-18, MLS-161), so it comes first. CF-Connecting-IP, then the first
     * X-Forwarded-For entry and remoteAddr only serve outside App Platform (dev, tests).
     */
    @JvmStatic
    fun getIpAddress(request: HttpServletRequest): String? =
        request.getHeader("do-connecting-ip")?.trim()?.takeIf { it.isNotEmpty() }
            ?: request.getHeader("CF-Connecting-IP")?.trim()
            ?: request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr

}
