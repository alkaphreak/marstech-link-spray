package fr.marstech.mtlinkspray.utils

import fr.marstech.mtlinkspray.enums.HttpDefaultPortEnum
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

    @JvmStatic
    fun getIpAddress(request: HttpServletRequest): String? =
        request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull() ?: request.remoteAddr

}
