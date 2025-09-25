package fr.marstech.mtlinkspray.controller.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.service.DashboardService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(DashboardApiController::class)
class DashboardApiControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var dashboardService: DashboardService

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `should get dashboard`() {
        val dashboard = DashboardDto()
        whenever(dashboardService.getDashboard("1")).thenReturn(dashboard)

        mockMvc.perform(get("/api/dashboard/1"))
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(dashboard)))
    }

    @Test
    fun `should create dashboard`() {
        val dashboard = DashboardDto()
        whenever(dashboardService.createDashboard(any<DashboardDto>())).thenReturn(dashboard)

        mockMvc.perform(
            post("/api/dashboard")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dashboard))
        ).andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(dashboard)))
    }

    @Test
    fun `should update dashboard`() {
        val dashboard = DashboardDto()
        whenever(dashboardService.updateDashboard(eq("1"), any<DashboardDto>())).thenReturn(dashboard)

        mockMvc.perform(
            put("/api/dashboard/1")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dashboard))
        ).andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(dashboard)))
    }
}