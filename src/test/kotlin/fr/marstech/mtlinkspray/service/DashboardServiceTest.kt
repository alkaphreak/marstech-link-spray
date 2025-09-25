package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.config.TestConfig
import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.entity.DashboardItem
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
class DashboardServiceTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val mongoDBContainer = MongoDBContainer(DockerImageName.parse(TestConfig.MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true)
    }

    @Autowired
    lateinit var dashboardService: DashboardService

    @Test
    fun `should create dashboard`() {
        val items: MutableList<DashboardItem> = mutableListOf()
        val dashboardDto =
            DashboardDto(
                id = UUID.randomUUID().toString(),
                name = "Test Dashboard",
                items = items,
                description = "This is a test dashboard"
            )
        val createdDashboard = dashboardService.createDashboard(dashboardDto)
        assertNotNull(createdDashboard)
        assertEquals(dashboardDto.name, createdDashboard.name)
        assertEquals(dashboardDto.description, createdDashboard.description)
        assertEquals(dashboardDto.items, createdDashboard.items)
        assertNotNull(createdDashboard.id)
    }

    @Test
    fun `should create dashboard with name`() {
        val dashboardName = "Test Dashboard"
        val createdDashboard = dashboardService.createDashboard(dashboardName)
        assertNotNull(createdDashboard)
        assertEquals(dashboardName, createdDashboard.name)
        assertNotNull(createdDashboard.id)
    }

    @Test
    fun `should get dashboard`() {
        val items: MutableList<DashboardItem> = mutableListOf()
        val dashboardDto =
            DashboardDto(
                id = UUID.randomUUID().toString(),
                name = "Test Dashboard",
                items = items,
                description = "This is a test dashboard"
            )
        val createdDashboard = dashboardService.createDashboard(dashboardDto)
        val retrievedDashboard = dashboardService.getDashboard(createdDashboard.id)
        assertNotNull(retrievedDashboard)
        assertEquals(createdDashboard.id, retrievedDashboard.id)
        assertEquals(createdDashboard.name, retrievedDashboard.name)
        assertEquals(createdDashboard.description, retrievedDashboard.description)
        assertEquals(createdDashboard.items, retrievedDashboard.items)
    }

    @Test
    fun `should throw when dashboard not found`() {
        assertThrows<NoSuchElementException> {
            dashboardService.getDashboard("non-existent-id")
        }
    }

    @Test
    fun `should return null when updating dashboard`() {
        val items: MutableList<DashboardItem> = mutableListOf()
        val dashboardDto =
            DashboardDto(
                id = UUID.randomUUID().toString(),
                name = "Test Dashboard",
                items = items,
                description = "This is a test dashboard"
            )
        val createdDashboard = dashboardService.createDashboard(dashboardDto)
        val updatedDashboard = dashboardService.updateDashboard(createdDashboard.id, dashboardDto)
        assertNull(updatedDashboard)
    }
}