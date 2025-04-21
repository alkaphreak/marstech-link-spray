package fr.marstech.mtlinkspray.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class NetworkUtils {

    public static Map<String, String> getHeadersAsMap(HttpServletRequest httpServletRequest) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = httpServletRequest.getHeader(key);
            headers.put(key, value);
        }
        return headers;
    }

    public static String filterDefaultPort(String port) {
        if (port == null) {
            return null;
        }
        if (port.equals("80") || port.equals("443")) {
            return null;
        }
        return port;
    }

    public static String getPort(HttpServletRequest httpServletRequest) {
        return getPort(httpServletRequest, getHeadersAsMap(httpServletRequest));
    }

    private static String getPort(HttpServletRequest httpServletRequest, Map<String, String> headers) {
        String res = headers.getOrDefault("x-forwarded-port", null);
        if (res == null) {
            res = headers.getOrDefault("port", null);
        }
        if (res == null) {
            res = String.valueOf(httpServletRequest.getServerPort());
        }
        return res;
    }

    public static String getScheme(HttpServletRequest httpServletRequest) {
        return getScheme(httpServletRequest, getHeadersAsMap(httpServletRequest));
    }

    private static String getScheme(HttpServletRequest httpServletRequest, Map<String, String> headers) {
        String res = headers.getOrDefault("x-forwarded-proto", null);
        if (res == null) {
            res = httpServletRequest.getScheme();
        }
        return res;
    }

    public static String getHost(HttpServletRequest httpServletRequest) {
        return getHost(httpServletRequest, getHeadersAsMap(httpServletRequest));
    }

    private static String getHost(HttpServletRequest httpServletRequest, Map<String, String> headers) {
        String res;
        res = headers.getOrDefault("x-forwarded-server", null);
        if (res == null) {
            res = headers.getOrDefault("x-forwarded-host", null);
        }
        if (res == null) {
            res = headers.getOrDefault("host", null);
        }
        if (res == null) {
            res = httpServletRequest.getServerName();
        }
        return res.split(":")[0];
    }

    public static Boolean isValidUrl(String url) {
        try {
            return StringUtils.isNotBlank(new URI(url).toURL().toString());
        } catch (Exception e) {
            return false;
        }
    }
}
