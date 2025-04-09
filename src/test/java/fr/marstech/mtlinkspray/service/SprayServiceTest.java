package fr.marstech.mtlinkspray.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@Testcontainers
@SpringBootTest
class SprayServiceTest {

    @Autowired
    SprayService sprayService;

    @MockitoBean
    HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        when(httpServletRequest.getHeaderNames()).thenReturn(new Enumeration<>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public String nextElement() {
                return "";
            }
        });
        when(httpServletRequest.getServerName()).thenReturn("localhost");
        when(httpServletRequest.getServerPort()).thenReturn(8080);
        when(httpServletRequest.getScheme()).thenReturn("http");
        when(httpServletRequest.getRequestURI()).thenReturn("spray");
    }

    @Test
    void testGetLinkList() {
        String inputLinkList = """
                https://www.google.com
                https://www.facebook.com
                https://www.twitter.com""";
        var linkList = SprayService.getLinkList(inputLinkList);
        assertEquals(3, linkList.size());
        assertEquals("https://www.google.com", linkList.get(0));
        assertEquals("https://www.facebook.com", linkList.get(1));
        assertEquals("https://www.twitter.com", linkList.get(2));
    }

    @Test
    void testGetLinkListText() {
        String inputLinkList = """
                https://www.google.com
                https://www.facebook.com
                https://www.twitter.com""";
        var linkList = SprayService.getLinkList(inputLinkList);
        var linkListText = SprayService.getLinkListText(linkList);
        assertEquals(inputLinkList, linkListText);
    }

    @Test
    void testGetLinkSpray() {
        String inputLinkList = """
                https://www.google.com
                https://www.facebook.com
                https://www.twitter.com""";
        var linkList = SprayService.getLinkList(inputLinkList);
        var linkSpray = SprayService.getLinkSpray(httpServletRequest, linkList);
        assertEquals("http://localhost:8080/spray/open?spray=https://www.google.com&spray=https://www.facebook.com&spray=https://www.twitter.com", linkSpray);
    }
}