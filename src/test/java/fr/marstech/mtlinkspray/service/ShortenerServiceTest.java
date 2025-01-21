package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.entity.LinkItem;
import fr.marstech.mtlinkspray.repository.LinkItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;

import static fr.marstech.mtlinkspray.config.TestConfig.MONGO_DB_DOCKER_IMAGE_NAME;
import static fr.marstech.mtlinkspray.utils.NetworkUtils.isValidUrl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@Testcontainers
@SpringBootTest
class ShortenerServiceTest {

    @Autowired
    LinkItemRepository linkItemRepository;

    @Autowired
    ShortenerService shortenerService;

    @MockBean
    HttpServletRequest httpServletRequest;

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse(MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true);

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
        String result = shortenerService.shorten(validUrl, httpServletRequest);
        assertTrue(isValidUrl(result));
    }

    @Test
    void shortenInvalidUrl() {
        String invalidUrl = "invalid-url";
        Exception exception = assertThrows(RuntimeException.class, () -> shortenerService.shorten(invalidUrl, httpServletRequest));
        assertInstanceOf(MalformedURLException.class, exception.getCause());
    }
}