package fr.marstech.mtlinkspray.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
@Log
public class RootController {

    private static Map<String, String> getHeadersAsMap(HttpServletRequest httpServletRequest) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = httpServletRequest.getHeader(key);
            headers.put(key, value);
        }
        return headers;
    }

    private static String getPort(
            HttpServletRequest httpServletRequest,
            Map<String, String> headers
    ) {

        String res = headers.getOrDefault("x-forwarded-port", null);
        log.info("x-forwarded-port:" + res);
        if (res == null) {
            res = String.valueOf(httpServletRequest.getServerPort());
            log.info("getServerPort:" + res);
        }
        return res;
    }

    private static String getScheme(
            HttpServletRequest httpServletRequest,
            Map<String, String> headers
    ) {
        String res = headers.getOrDefault("x-forwarded-proto", null);
        log.info("x-forwarded-proto:" + res);
        if (res == null) {
            res = httpServletRequest.getScheme();
            log.info("getScheme:" + res);
        }
        return res;
    }

    private static String getHost(
            HttpServletRequest httpServletRequest,
            Map<String, String> headers
    ) {
        String res;
        res = headers.getOrDefault("x-forwarded-server", null);
        log.info("x-forwarded-server:" + res);

        if (res == null) {
            res = headers.getOrDefault("x-forwarded-host", null);
            log.info("x-forwarded-host:" + res);
        }
        if (res == null) {
            res = headers.getOrDefault("host", null);
            log.info("host:" + res);
        }
        if (res == null) {
            res = httpServletRequest.getServerName();
            log.info("getServerName:" + res);
        }

        return res.split(":")[0];
    }

    static List<String> getLinkList(String inputLinkList) {
        return inputLinkList.trim().lines()
                .filter(Predicate.not(String::isBlank))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    static String getLinkListText(List<String> linkList) {
        return CollectionUtils.isNotEmpty(linkList) ?
                String.join("\n", linkList) :
                null;
    }

    @GetMapping("/")
    public ModelAndView getHome(
            HttpServletRequest httpServletRequest
    ) {
        return new ModelAndView("index")
                .addObject("headers", getHeadersAsMap(httpServletRequest));
    }

    @PostMapping("/")
    public ModelAndView getLink(
            @RequestParam("inputLinkList") String inputLinkList,
            HttpServletRequest httpServletRequest
    ) {
        List<String> linkList = getLinkList(inputLinkList);
        String linkListText = getLinkListText(linkList);
        String linkSpray = getLinkSpray(httpServletRequest, linkList);

        return new ModelAndView("index")
                .addObject("linkList", linkList)
                .addObject("linkListText", linkListText)
                .addObject("linkSpray", linkSpray);
    }

    static String getLinkSpray(
            HttpServletRequest httpServletRequest,
            List<String> linkList
    ) {
        if (CollectionUtils.isNotEmpty(linkList)) {
            Map<String, String> headers = getHeadersAsMap(httpServletRequest);
            String host = getHost(httpServletRequest, headers);
            String scheme = getScheme(httpServletRequest, headers);
            String port = getPort(httpServletRequest, headers);
            return UriComponentsBuilder
                    .newInstance()
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

    @GetMapping("/open")
    public ModelAndView getSpray(
            @RequestParam List<String> spray
    ) {
        return new ModelAndView("index")
                .addObject("linkListText", getLinkListText(spray))
                .addObject("spray", spray);
    }
}
