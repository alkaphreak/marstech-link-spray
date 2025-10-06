package fr.marstech.mtlinkspray.controller.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.service.DashboardService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class DashboardApiControllerTest {
    private lateinit var mockMvc: MockMvc
    private val dashboardService = mock<DashboardService>()
    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        val validator = org.springframework.validation.beanvalidation.LocalValidatorFactoryBean()
        validator.afterPropertiesSet()
        
        mockMvc = MockMvcBuilders.standaloneSetup(DashboardApiController(dashboardService))
            .setControllerAdvice(fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler())
            .setValidator(validator)
            .build()
    }

    @Test
    fun shouldGetDashboard() {
        val dashboard = DashboardDto()
        whenever(dashboardService.getDashboard("1")).thenReturn(dashboard)

        mockMvc.perform(get("/api/dashboard/1")).andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(dashboard)))
    }

    @Test
    fun shouldCreateDashboard() {
        val dashboard = DashboardDto()
        whenever(dashboardService.createDashboard(any<DashboardDto>())).thenReturn(dashboard)

        mockMvc.perform(
            post("/api/dashboard").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(dashboard))
        ).andExpect(status().isOk).andExpect(content().json(objectMapper.writeValueAsString(dashboard)))
    }

    @Test
    fun shouldUpdateDashboard() {
        val dashboard = DashboardDto()
        whenever(dashboardService.updateDashboard(eq("1"), any<DashboardDto>())).thenReturn(dashboard)

        mockMvc.perform(
            put("/api/dashboard/1").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(dashboard))
        ).andExpect(status().isOk).andExpect(content().json(objectMapper.writeValueAsString(dashboard)))
    }

    @Test
    fun shouldReturn400ForBlankId() {
        // Test with whitespace string - this should trigger our validation
        whenever(dashboardService.getDashboard(" "))
            .thenThrow(IllegalArgumentException("ID cannot be blank"))
        
        mockMvc.perform(get("/api/dashboard/ "))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturn404ForNonExistentDashboard() {
        whenever(dashboardService.getDashboard("nonexistent"))
            .thenThrow(NoSuchElementException("Dashboard not found"))

        mockMvc.perform(get("/api/dashboard/nonexistent"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun shouldReturn400ForInvalidJson() {
        mockMvc.perform(
            post("/api/dashboard")
                .contentType(APPLICATION_JSON)
                .content("{invalid json}")
        ).andExpect(status().isBadRequest)
    }
}