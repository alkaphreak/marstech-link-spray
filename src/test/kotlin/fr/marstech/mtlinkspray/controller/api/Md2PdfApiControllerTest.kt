package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import fr.marstech.mtlinkspray.service.MarkdownToPdfService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [Md2PdfApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class Md2PdfApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var markdownToPdfService: MarkdownToPdfService

    private fun givenRequest() = post("/api/md2pdf")
        .contentType("text/markdown")
        .header("X-Api-Key", "key123.secret")
        .content("# Hello")

    @Test
    fun shouldReturnPdfAsAttachment() {
        // Given
        val pdf = "%PDF-1.7 fake".toByteArray()
        whenever(markdownToPdfService.render(any(), any())).thenReturn(pdf)

        // When / Then
        mockMvc.perform(givenRequest())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"document.pdf\""))
            .andExpect(content().bytes(pdf))
    }

    @Test
    fun shouldReturnUnauthorizedWhenApiKeyIsInvalid() {
        // Given
        doThrow(IllegalAccessException("Invalid API key")).whenever(markdownToPdfService).render(any(), any())

        // When / Then
        mockMvc.perform(givenRequest()).andExpect(status().isUnauthorized)
    }

    @Test
    fun shouldReturnContentTooLargeWhenMarkdownExceedsMaxSize() {
        // Given
        doThrow(PayloadTooLargeException("too large")).whenever(markdownToPdfService).render(any(), any())

        // When / Then
        mockMvc.perform(givenRequest()).andExpect(status().isPayloadTooLarge)
    }

    @Test
    fun shouldReturnBadRequestWhenMarkdownIsBlank() {
        // Given
        doThrow(IllegalArgumentException("Markdown body is empty")).whenever(markdownToPdfService).render(any(), any())

        // When / Then
        mockMvc.perform(givenRequest()).andExpect(status().isBadRequest)
    }

    @Test
    fun shouldRejectUnsupportedContentType() {
        // When / Then
        mockMvc.perform(givenRequest().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnsupportedMediaType)
    }
}
