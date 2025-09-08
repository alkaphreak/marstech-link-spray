package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.entity.HistoryItem;
import fr.marstech.mtlinkspray.entity.LinkItem;
import fr.marstech.mtlinkspray.entity.LinkItemTarget;
import fr.marstech.mtlinkspray.repository.LinkItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.*;

import static fr.marstech.mtlinkspray.config.TestConfig.MONGO_DB_DOCKER_IMAGE_NAME;
import static fr.marstech.mtlinkspray.utils.NetworkUtils.isValidUrl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
class ShortenerServiceTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer =
      new MongoDBContainer(DockerImageName.parse(MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true);

  @Autowired LinkItemRepository linkItemRepository;
  @Autowired ShortenerService shortenerService;
  @MockitoBean HttpServletRequest httpServletRequest;

  @BeforeEach
  void setUp() {
    when(httpServletRequest.getHeaderNames())
        .thenReturn(
            new Enumeration<>() {
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

  @Test
  void linkItemTest() {
    String id = UUID.randomUUID().toString();
    LinkItem linkItem =
        linkItemRepository.save(
            new LinkItem(
                id,
                LocalDateTime.now(),
                null,
                true,
                null,
                Map.of(),
                new HistoryItem(),
                List.of(),
                new LinkItemTarget("https://www.example.com")));
    Optional<LinkItem> byId = linkItemRepository.findById(id);
    //noinspection OptionalGetWithoutIsPresent
    assertEquals(linkItem.getId(), byId.get().getId());
  }

  @Test
  void shortenValidUrl() {
    String validUrl = "https://www.example.com";
    String result = shortenerService.shorten(validUrl, httpServletRequest);
    assertTrue(isValidUrl(result));
  }

  @Test
  void shortenEmptyUrl() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> shortenerService.shorten(null, httpServletRequest));
    assertInstanceOf(IllegalArgumentException.class, exception);
    assertEquals("URL must not be null or empty", exception.getMessage());
  }

  @Test
  void shortenInvalidUrl() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> shortenerService.shorten("invalid-url", httpServletRequest));
    assertInstanceOf(IllegalArgumentException.class, exception);
    assertEquals("Invalid URL: invalid-url", exception.getMessage());
  }
}
