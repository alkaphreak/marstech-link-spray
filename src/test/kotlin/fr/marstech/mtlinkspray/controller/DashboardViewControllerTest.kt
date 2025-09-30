package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.view.DashboardViewController
import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.service.DashboardService
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class DashboardViewControllerTest {

    private lateinit var dashboardService: DashboardService
    private lateinit var controller: DashboardViewController
    private lateinit var request: HttpServletRequest

    @BeforeEach
    fun setUp() {
        dashboardService = mock(DashboardService::class.java)
        controller = DashboardViewController(dashboardService)
        request = mock(HttpServletRequest::class.java)
    }

    @Test
    fun `getView should return ModelAndView`() {
        val result = controller.getView(request)
        assertTrue(result.viewName == "dashboard")
    }

    @Test
    fun `postView should create dashboard and redirect`() {
        val dto = DashboardDto("123", "Test Dashboard")
        `when`(dashboardService.createDashboard("Test Dashboard")).thenReturn(dto)

        val redirectUrl = controller.postView("Test Dashboard")
        assertEquals("redirect:/dashboard/123", redirectUrl)
    }

    @Test
    fun `getDashboardById should add dashboard to model`() {
        val dashboard = DashboardDto("456", "Another Dashboard")
        `when`(dashboardService.getDashboard("456")).thenReturn(dashboard)

        val result = controller.getDashboardById("456", request)
        assertEquals(dashboard, result.model["dashboard"])
    }
}