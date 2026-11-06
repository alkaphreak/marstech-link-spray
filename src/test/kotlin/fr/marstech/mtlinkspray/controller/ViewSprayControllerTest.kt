package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.view.ViewSprayController
import fr.marstech.mtlinkspray.entity.WriteSignatureEntity.ResourceType.SHORT_LINK
import fr.marstech.mtlinkspray.entity.WriteSignatureEntity.ResourceType.SPRAY
import fr.marstech.mtlinkspray.service.SprayService
import fr.marstech.mtlinkspray.service.WriteSignatureService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ViewSprayControllerTest {

    private val sprayService: SprayService = mock(SprayService::class.java)
    private val writeSignatureService: WriteSignatureService = mock(WriteSignatureService::class.java)
    private val controller = ViewSprayController(sprayService, writeSignatureService)

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

    @Test
    fun postShortenedSprayShouldReturnCorrectViewWithShortenedLink() {
        // Given
        val request = MockHttpServletRequest()
        val shortenedUrl = "http://localhost/abc123"
        val childUrl = "http://localhost/child1"
        val fp = "ab".repeat(32)
        whenever(sprayService.shortenAndSpray(eq(listOf("https://example.com")), eq(request), any()))
            .thenAnswer { invocation ->
                invocation.getArgument<(String) -> Unit>(2).invoke(childUrl)
                shortenedUrl
            }

        // When
        val result: ModelAndView = controller.getShortenedSpray("https://example.com", fp, request)

        // Then: the child short link is signed as well as the spray
        assertEquals("spray", result.viewName)
        assertNotNull(result.model["linkList"])
        assertNotNull(result.model["linkListText"])
        assertEquals(shortenedUrl, result.model["linkSpray"])
        assertEquals(true, result.model["linkSprayShortened"])
        verify(writeSignatureService).record(SHORT_LINK, childUrl, fp, request)
        verify(writeSignatureService).record(SPRAY, shortenedUrl, fp, request)
    }
}