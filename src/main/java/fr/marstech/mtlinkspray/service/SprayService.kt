package fr.marstech.mtlinkspray.service;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.function.Predicate;

import static fr.marstech.mtlinkspray.utils.NetworkUtils.*;
import static java.text.MessageFormat.format;

public interface SprayService {

    /**
     * Get the list of links from the input String
     *
     * @param inputLinkList the input String
     * @return the list of links
     */
    static List<String> getLinkList(String inputLinkList) {
        return inputLinkList.trim().lines().filter(Predicate.not(String::isBlank)).map(String::trim).toList();
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
            String host = getHost(httpServletRequest);
            String scheme = getScheme(httpServletRequest);
            String port = filterDefaultPort(getPort(httpServletRequest));
            return UriComponentsBuilder.newInstance().scheme(scheme).host(host).port(port).path(format("{0}/open", httpServletRequest.getRequestURI())).queryParam("spray", linkList).build().encode().toString();
        }
        return null;
    }
}