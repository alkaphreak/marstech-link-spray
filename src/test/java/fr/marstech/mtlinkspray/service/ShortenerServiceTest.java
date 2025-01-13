package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.config.AbstractBaseIntegrationTest;
import fr.marstech.mtlinkspray.entity.LinkItem;
import fr.marstech.mtlinkspray.repository.LinkItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import static fr.marstech.mtlinkspray.utils.NetworkUtils.isValidUrl;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class ShortenerServiceTest extends AbstractBaseIntegrationTest {

    @Autowired
    LinkItemRepository linkItemRepository;

    @Autowired
    ShortenerService shortenerService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void linkItemTest() {
        String id = UUID.randomUUID().toString();
        LinkItem linkItem = linkItemRepository.save(new LinkItem().setId(id));
        Optional<LinkItem> byId = linkItemRepository.findById(id);
        //noinspection OptionalGetWithoutIsPresent
        assertEquals(linkItem.getId(), byId.get().getId());
    }

    @Test
    void shortenValidUrl() throws MalformedURLException {
        String validUrl = "https://www.example.com";
        URL result = shortenerService.shorten(validUrl);
        assertTrue(isValidUrl(result.toString()));
    }

    @Test
    void shortenInvalidUrl() {
        String invalidUrl = "invalid-url";
        Exception exception = assertThrows(RuntimeException.class, () -> shortenerService.shorten(invalidUrl));
        assertInstanceOf(MalformedURLException.class, exception.getCause());
    }
}