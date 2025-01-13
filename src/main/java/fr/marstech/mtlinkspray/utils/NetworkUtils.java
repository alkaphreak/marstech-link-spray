package fr.marstech.mtlinkspray.utils;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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

    public static String getPort(HttpServletRequest httpServletRequest, Map<String, String> headers) {
        String res = headers.getOrDefault("x-forwarded-port", null);
        if (res == null) {
            res = headers.getOrDefault("port", null);
        }
        if (res == null) {
            res = String.valueOf(httpServletRequest.getServerPort());
        }
        return res;
    }

    public static String getScheme(HttpServletRequest httpServletRequest, Map<String, String> headers) {
        String res = headers.getOrDefault("x-forwarded-proto", null);
        if (res == null) {
            res = httpServletRequest.getScheme();
        }
        return res;
    }

    public static String getHost(HttpServletRequest httpServletRequest, Map<String, String> headers) {
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
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }
}
