package fr.marstech.mtlinkspray.service;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static fr.marstech.mtlinkspray.utils.NetworkUtils.*;
import static fr.marstech.mtlinkspray.utils.NetworkUtils.getPort;

public interface SprayService {

    /**
     * Get the list of links from the input String
     *
     * @param inputLinkList the input String
     * @return the list of links
     */
    static List<String> getLinkList(String inputLinkList) {
        return inputLinkList.trim().lines().filter(Predicate.not(String::isBlank)).map(String::trim).collect(Collectors.toList());
    }

    /**
     * Get the text representation of the list of links
     *
     * @param linkList the list of links
     * @return the text representation of the list of links
     */
    static String getLinkListText(List<String> linkList) {
        return CollectionUtils.isNotEmpty(linkList) ? String.join("\n", linkList) : null;
    }

    /**
     * Get the link to the spray page opener
     *
     * @param httpServletRequest the HttpServletRequest
     * @param linkList           the list of links
     * @return the link to the spray page
     */
    static String getLinkSpray(HttpServletRequest httpServletRequest, List<String> linkList) {
        if (CollectionUtils.isNotEmpty(linkList)) {
            Map<String, String> headers = getHeadersAsMap(httpServletRequest);
            String host = getHost(httpServletRequest, headers);
            String scheme = getScheme(httpServletRequest, headers);
            String port = getPort(httpServletRequest, headers);
            return UriComponentsBuilder.newInstance()
                    .scheme(scheme)
                    .host(host)
                    .port(port)
                    .path(httpServletRequest.getRequestURI() + "/open")
                    .queryParam("spray", linkList)
                    .build()
                    .encode()
                    .toString();
        }
        return null;
    }
}