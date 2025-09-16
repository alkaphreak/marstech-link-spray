import fr.marstech.mtlinkspray.controller.DashboardController
import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.service.DashboardService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.web.servlet.ModelAndView
import jakarta.servlet.http.HttpServletRequest

class DashboardControllerTest {

    private lateinit var dashboardService: DashboardService
    private lateinit var controller: DashboardController
    private lateinit var request: HttpServletRequest

    @BeforeEach
    fun setUp() {
        dashboardService = mock(DashboardService::class.java)
        controller = DashboardController(dashboardService)
        request = mock(HttpServletRequest::class.java)
    }

    @Test
    fun `getView should return ModelAndView`() {
        val result = controller.getView(request)
        assertTrue(result is ModelAndView)
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