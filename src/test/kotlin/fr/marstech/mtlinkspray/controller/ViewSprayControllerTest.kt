package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.view.ViewSprayController
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ViewSprayControllerTest {

    private val controller = ViewSprayController()

    @Test
    fun getSprayPageShouldReturnCorrectView() {
        val request = MockHttpServletRequest()
        val result: ModelAndView = controller.getSprayPage(request)

        assertEquals("spray", result.viewName)
        assertNotNull(result.model["headers"])
    }

    @Test
    fun postSprayShouldReturnCorrectViewWithModel() {
        val request = MockHttpServletRequest()
        val result: ModelAndView = controller.getLink("https://example.com", request)

        assertEquals("spray", result.viewName)
        assertNotNull(result.model["linkList"])
        assertNotNull(result.model["linkListText"])
        assertNotNull(result.model["linkSpray"])
    }

    @Test
    fun postSprayShouldReturnEmptyLinkListWhenInputIsBlank() {
        // Given
        val request = MockHttpServletRequest()

        // When
        val result: ModelAndView = controller.getLink("   ", request)

        // Then
        assertEquals("spray", result.viewName)
        val linkList = result.model["linkList"] as? List<*>
        assertNotNull(linkList)
        assertTrue(linkList.isEmpty())
    }

    @Test
    fun getSprayOpenShouldReturnCorrectViewWithModel() {
        val sprayList = listOf("https://example.com")
        val result: ModelAndView = controller.getSpray(sprayList)

        assertEquals("spray", result.viewName)
        assertNotNull(result.model["linkListText"])
        assertNotNull(result.model["spray"])
    }
}