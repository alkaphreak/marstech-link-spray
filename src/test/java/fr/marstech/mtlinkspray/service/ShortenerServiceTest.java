package fr.marstech.mtlinkspray.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class ShortenerServiceTest {

    ShortenerService shortenerService = new ShortenerService();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shortenValidUrl() {
        String validUrl = "https://www.example.com";
        URL result = shortenerService.shorten(validUrl);
        assertNotNull(result);
        assertEquals("https://www.example.com", result.toString());
    }

    @Test
    void shortenInvalidUrl() {
        String invalidUrl = "invalid-url";
        Exception exception = assertThrows(RuntimeException.class, () -> shortenerService.shorten(invalidUrl));
        assertInstanceOf(MalformedURLException.class, exception.getCause());
    }
}