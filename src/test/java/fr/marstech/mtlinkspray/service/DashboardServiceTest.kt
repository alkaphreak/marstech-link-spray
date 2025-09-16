package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.dto.DashboardDto;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static fr.marstech.mtlinkspray.config.TestConfig.MONGO_DB_DOCKER_IMAGE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@Log
@Testcontainers
@SpringBootTest
class DashboardServiceTest {

  @Container @ServiceConnection
  static MongoDBContainer mongoDBContainer =
      new MongoDBContainer(DockerImageName.parse(MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true);

  @Autowired private DashboardService dashboardService;

  @Test
  void should_createDashboard() {
    // Given
    DashboardDto dashboardDto =
        new DashboardDto(
            UUID.randomUUID().toString(), "Test Dashboard", List.of(), "This is a test dashboard");

    // When
    DashboardDto createdDashboard = dashboardService.createDashboard(dashboardDto);

    // Then
    assertNotNull(createdDashboard);
    assertEquals(dashboardDto.getName(), createdDashboard.getName());
    assertEquals(dashboardDto.getDescription(), createdDashboard.getDescription());
    assertEquals(dashboardDto.getItems(), createdDashboard.getItems());
    assertNotNull(createdDashboard.getId());
  }

  @Test
  void should_createDashboardWithName() {
    // Given
    String dashboardName = "Test Dashboard";

    // When
    DashboardDto createdDashboard = dashboardService.createDashboard(dashboardName);

    // Then
    assertNotNull(createdDashboard);
    assertEquals(dashboardName, createdDashboard.getName());
    assertNotNull(createdDashboard.getId());
  }

  @Test
  void should_getDashboard() {
    // Given
    DashboardDto dashboardDto =
        new DashboardDto(
            UUID.randomUUID().toString(), "Test Dashboard", List.of(), "This is a test dashboard");
    DashboardDto createdDashboard = dashboardService.createDashboard(dashboardDto);

    // When
    DashboardDto retrievedDashboard = dashboardService.getDashboard(createdDashboard.getId());

    // Then
    assertNotNull(retrievedDashboard);
    assertEquals(createdDashboard.getId(), retrievedDashboard.getId());
    assertEquals(createdDashboard.getName(), retrievedDashboard.getName());
    assertEquals(createdDashboard.getDescription(), retrievedDashboard.getDescription());
    assertEquals(createdDashboard.getItems(), retrievedDashboard.getItems());
  }
}
