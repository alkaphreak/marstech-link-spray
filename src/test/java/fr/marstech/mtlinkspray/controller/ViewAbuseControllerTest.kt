package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.view.ViewAbuseController
import fr.marstech.mtlinkspray.enums.ViewNameEnum
import fr.marstech.mtlinkspray.service.ReportAbuseService
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ViewAbuseControllerTest {

    private val reportAbuseService: ReportAbuseService = mock()
    private val httpServletRequest: HttpServletRequest = mock()
    private val controller = ViewAbuseController(reportAbuseService)

    @Test
    fun `getView should return ModelAndView with correct view and headers`() {
        val modelAndView = controller.getView(httpServletRequest)
        assertEquals(ViewNameEnum.ABUSE.viewName, modelAndView.viewName)
        assertTrue(modelAndView.model.containsKey("headers"))
        assertTrue(modelAndView.model.containsKey("viewNameEnum"))
        assertEquals(ViewNameEnum.ABUSE, modelAndView.model["viewNameEnum"])
    }

    @Test
    fun `getLink should call reportAbuse and return ModelAndView with correct view and headers`() {
        val inputAbuseDescription = "test abuse"
        val modelAndView = controller.postAbuseForm(inputAbuseDescription, httpServletRequest)
        verify(reportAbuseService).reportAbuse(eq(inputAbuseDescription), eq(httpServletRequest))
        assertEquals(ViewNameEnum.ABUSE.viewName, modelAndView.viewName)
        assertTrue(modelAndView.model.containsKey("headers"))
        assertTrue(modelAndView.model.containsKey("viewNameEnum"))
        assertEquals(ViewNameEnum.ABUSE, modelAndView.model["viewNameEnum"])
    }
}