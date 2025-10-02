package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.entity.DashboardEntity
import fr.marstech.mtlinkspray.entity.DashboardItem
import fr.marstech.mtlinkspray.entity.DashboardLink
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.repository.DashboardRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
class DashboardServiceTest {
    private val dashboardRepository = mock(DashboardRepository::class.java)
    private val dashboardService = DashboardServiceImpl(dashboardRepository)

    @Test
    fun shouldCreateDashboard() {
        val items: MutableList<DashboardItem> = mutableListOf(
            DashboardLink(name = "Link 1", description = "desc", url = "https://example.com")
        )
        val dashboardDto = DashboardDto(
            id = UUID.randomUUID().toString(),
            name = "Test Dashboard",
            items = items,
            description = "This is a test dashboard"
        )
        val dashboardEntity = DashboardEntity(
            id = dashboardDto.id,
            creationDate = LocalDateTime.now(),
            author = HistoryItem("author"),
            name = dashboardDto.name,
            items = items,
            description = dashboardDto.description
        )
        `when`(dashboardRepository.save(org.mockito.kotlin.any())).thenReturn(dashboardEntity)
        `when`(dashboardRepository.findById(dashboardDto.id)).thenReturn(Optional.of(dashboardEntity))
        val createdDashboard = dashboardService.createDashboard(dashboardDto)
        assertNotNull(createdDashboard)
        assertEquals(dashboardDto.name, createdDashboard.name)
        assertEquals(dashboardDto.description, createdDashboard.description)
        assertEquals(dashboardDto.items, createdDashboard.items)
        assertNotNull(createdDashboard.id)
    }

    @Test
    fun shouldCreateDashboardWithName() {
        val dashboardName = "Test Dashboard"
        val dashboardEntity = DashboardEntity(
            id = UUID.randomUUID().toString(),
            creationDate = LocalDateTime.now(),
            author = HistoryItem("author"),
            name = dashboardName
        )
        `when`(dashboardRepository.save(org.mockito.kotlin.any())).thenReturn(dashboardEntity)
        val createdDashboard = dashboardService.createDashboard(dashboardName)
        assertNotNull(createdDashboard)
        assertEquals(dashboardName, createdDashboard.name)
        assertNotNull(createdDashboard.id)
    }

    @Test
    fun shouldGetDashboard() {
        val items: MutableList<DashboardItem> = mutableListOf()
        val dashboardDto = DashboardDto(
            id = UUID.randomUUID().toString(),
            name = "Test Dashboard",
            items = items,
            description = "This is a test dashboard"
        )
        val dashboardEntity = DashboardEntity(
            id = dashboardDto.id,
            creationDate = LocalDateTime.now(),
            author = HistoryItem("author"),
            name = dashboardDto.name,
            items = items,
            description = dashboardDto.description
        )
        `when`(dashboardRepository.save(org.mockito.kotlin.any())).thenReturn(dashboardEntity)
        `when`(dashboardRepository.findById(dashboardDto.id)).thenReturn(Optional.of(dashboardEntity))
        val createdDashboard = dashboardService.createDashboard(dashboardDto)
        val retrievedDashboard = dashboardService.getDashboard(createdDashboard.id)
        assertNotNull(retrievedDashboard)
        assertEquals(createdDashboard.id, retrievedDashboard.id)
        assertEquals(createdDashboard.name, retrievedDashboard.name)
        assertEquals(createdDashboard.description, retrievedDashboard.description)
        assertEquals(createdDashboard.items, retrievedDashboard.items)
    }

    @Test
    fun shouldThrowWhenDashboardNotFound() {
        assertThrows<NoSuchElementException> {
            dashboardService.getDashboard("non-existent-id")
        }
    }

    @Test
    fun shouldUpdateDashboard() {
        val items: MutableList<DashboardItem> = mutableListOf()
        val dashboardDto = DashboardDto(
            id = UUID.randomUUID().toString(),
            name = "Test Dashboard",
            items = items,
            description = "This is a test dashboard"
        )
        val dashboardEntity = DashboardEntity(
            id = dashboardDto.id,
            creationDate = LocalDateTime.now(),
            author = HistoryItem("author"),
            name = dashboardDto.name,
            items = items,
            description = dashboardDto.description
        )
        `when`(dashboardRepository.save(org.mockito.kotlin.any())).thenReturn(dashboardEntity)
        `when`(dashboardRepository.findById(dashboardDto.id)).thenReturn(Optional.of(dashboardEntity))
        val createdDashboard = dashboardService.createDashboard(dashboardDto)
        val updatedDashboard = dashboardService.updateDashboard(createdDashboard.id, dashboardDto)
        assertNotNull(updatedDashboard)
        assertEquals(createdDashboard.id, updatedDashboard.id)
        assertEquals(createdDashboard.name, updatedDashboard.name)
        assertEquals(createdDashboard.description, updatedDashboard.description)
        assertEquals(createdDashboard.items, updatedDashboard.items)
    }
}