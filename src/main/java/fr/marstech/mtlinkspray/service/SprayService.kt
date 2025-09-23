package fr.marstech.mtlinkspray.service

        import fr.marstech.mtlinkspray.utils.NetworkUtils.filterDefaultPort
        import fr.marstech.mtlinkspray.utils.NetworkUtils.getHost
        import fr.marstech.mtlinkspray.utils.NetworkUtils.getPort
        import fr.marstech.mtlinkspray.utils.NetworkUtils.getScheme
        import jakarta.servlet.http.HttpServletRequest
        import org.springframework.web.util.UriComponentsBuilder
        import java.text.MessageFormat

        interface SprayService {
            companion object {

                /**
                 * Parses a string into a list of non-blank, trimmed links.
                 */
                @JvmStatic
                fun getLinkList(inputLinkList: String): List<String> =
                    inputLinkList.lineSequence()
                        .map(String::trim)
                        .filter(String::isNotBlank)
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
                    linkList: List<String>?
                ): String? {
                    if (linkList.isNullOrEmpty()) return null
                    val host = getHost(httpServletRequest)
                    val scheme = getScheme(httpServletRequest)
                    val port = filterDefaultPort(getPort(httpServletRequest))
                    return UriComponentsBuilder.newInstance()
                        .scheme(scheme)
                        .host(host)
                        .port(port)
                        .path(MessageFormat.format("{0}/open", httpServletRequest.requestURI))
                        .queryParam("spray", *linkList.toTypedArray())
                        .build()
                        .encode()
                        .toString()
                }
            }
        }