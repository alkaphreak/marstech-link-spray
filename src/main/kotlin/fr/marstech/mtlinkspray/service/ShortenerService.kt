package fr.marstech.mtlinkspray.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.util.UriComponentsBuilder;

import static fr.marstech.mtlinkspray.utils.NetworkUtils.*;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public interface ShortenerService {

    /**
     * Get the target URL from the shortened UID
     *
     * @param uid                the shortened UID
     * @param httpServletRequest the request
     * @return the target URL
     */
    static String getShortenedLink(HttpServletRequest httpServletRequest, String uid) {
        if (isNotBlank(uid)) {
            String host = getHost(httpServletRequest);
            String scheme = getScheme(httpServletRequest);
            String port = getFilteredPort(httpServletRequest);
            return UriComponentsBuilder.newInstance().scheme(scheme).host(host).port(port).path(format("{0}", uid)).build().encode().toString();
        } else {
            return null;
        }
    }

    /**
     * Get the shortened UID from the target URL
     *
     * @param url                the target URL
     * @param httpServletRequest the request
     * @return the shortened UID
     */
    String shorten(String url, HttpServletRequest httpServletRequest);

    /**
     * Get the target URL from the shortened UID
     *
     * @param uid                the shortened UID
     * @param httpServletRequest the request
     * @return the target URL
     */
    String getTarget(String uid, HttpServletRequest httpServletRequest) throws ChangeSetPersister.NotFoundException;
}