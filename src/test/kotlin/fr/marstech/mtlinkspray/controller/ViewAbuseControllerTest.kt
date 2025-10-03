package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.view.ViewAbuseController
import fr.marstech.mtlinkspray.enums.ViewNameEnum
import fr.marstech.mtlinkspray.service.ReportAbuseService
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ViewAbuseControllerTest {

    private val reportAbuseService: ReportAbuseService = mock()
    private val httpServletRequest: HttpServletRequest = mock()
    private val controller = ViewAbuseController(reportAbuseService)

    @Test
    fun getViewShouldReturnModelAndViewWithCorrectViewAndHeaders() {
        val modelAndView = controller.getView(httpServletRequest)
        assertEquals(ViewNameEnum.ABUSE.viewName, modelAndView.viewName)
        assertTrue(modelAndView.model.containsKey("headers"))
        assertTrue(modelAndView.model.containsKey("viewNameEnum"))
        assertEquals(ViewNameEnum.ABUSE, modelAndView.model["viewNameEnum"])
    }

    @Test
    fun getLinkShouldCallReportAbuseAndReturnModelAndViewWithCorrectViewAndHeaders() {
        val inputAbuseDescription = "test abuse"
        val modelAndView = controller.postAbuseForm(inputAbuseDescription, httpServletRequest)
        verify(reportAbuseService).reportAbuse(any(), any())
        assertEquals(ViewNameEnum.ABUSE.viewName, modelAndView.viewName)
        assertTrue(modelAndView.model.containsKey("headers"))
        assertTrue(modelAndView.model.containsKey("viewNameEnum"))
        assertEquals(ViewNameEnum.ABUSE, modelAndView.model["viewNameEnum"])
    }
}