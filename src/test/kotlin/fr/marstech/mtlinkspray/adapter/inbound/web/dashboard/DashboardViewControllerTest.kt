package fr.marstech.mtlinkspray.adapter.inbound.web.dashboard
import fr.marstech.mtlinkspray.domain.dashboard.DashboardService
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
    fun getViewShouldReturnModelAndView() {
        val result = controller.getView(request)
        assertTrue(result.viewName == "dashboard")
    }

    @Test
    fun postViewShouldCreateDashboardAndRedirect() {
        val dto = DashboardDto("123", "Test Dashboard")
        `when`(dashboardService.createDashboard("Test Dashboard")).thenReturn(dto.toDashboard())

        val redirectUrl = controller.postView("Test Dashboard")
        assertEquals("redirect:/dashboard/123", redirectUrl)
    }

    @Test
    fun getDashboardByIdShouldAddDashboardToModel() {
        val dashboard = DashboardDto("456", "Another Dashboard")
        `when`(dashboardService.getDashboard("456")).thenReturn(dashboard.toDashboard())

        val result = controller.getDashboardById("456", request)
        assertEquals(dashboard, result.model["dashboard"])
    }
}