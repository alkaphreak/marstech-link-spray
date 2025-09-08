package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.repository.AbuseReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import jakarta.servlet.http.HttpServletRequest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static fr.marstech.mtlinkspray.config.TestConfig.MONGO_DB_DOCKER_IMAGE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
class ReportAbuseServiceImplTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer =
      new MongoDBContainer(DockerImageName.parse(MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true);

  @Autowired public AbuseReportRepository abuseReportRepository;
  @Autowired private ReportAbuseServiceImpl reportAbuseService;

  ReportAbuseServiceImplTest() {}

  @BeforeEach
  void setUp() {
    abuseReportRepository.deleteAll();
  }

  @Test
  void reportAbuse() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    String abuse = "abuse";
    reportAbuseService.reportAbuse(abuse, request);
    assertEquals(1, abuseReportRepository.count());
    assertEquals("reportAbuse:abuse", abuseReportRepository.findAll().getFirst().getDescription());
  }
}
