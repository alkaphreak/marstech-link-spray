package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import fr.marstech.mtlinkspray.service.PdfToMarkdownService
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

@WebMvcTest(controllers = [Pdf2MdApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class Pdf2MdApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var pdfToMarkdownService: PdfToMarkdownService

    private fun givenRequest() = post("/api/pdf2md")
        .contentType(MediaType.APPLICATION_PDF)
        .header("X-Api-Key", "key123.secret")
        .content("%PDF-1.7 fake".toByteArray())

    @Test
    fun shouldReturnMarkdown() {
        // Given
        whenever(pdfToMarkdownService.convert(any(), any())).thenReturn("# Évolution\n")

        // When / Then
        mockMvc.perform(givenRequest())
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/markdown;charset=UTF-8"))
            .andExpect(content().string("# Évolution\n"))
    }

    @Test
    fun shouldReturnUnauthorizedWhenApiKeyIsInvalid() {
        // Given
        doThrow(IllegalAccessException("Invalid API key")).whenever(pdfToMarkdownService).convert(any(), any())

        // When / Then
        mockMvc.perform(givenRequest()).andExpect(status().isUnauthorized)
    }

    @Test
    fun shouldReturnContentTooLargeWhenPdfExceedsLimits() {
        // Given
        doThrow(PayloadTooLargeException("too large")).whenever(pdfToMarkdownService).convert(any(), any())

        // When / Then
        mockMvc.perform(givenRequest()).andExpect(status().isPayloadTooLarge)
    }

    @Test
    fun shouldReturnBadRequestWhenPdfIsUnreadable() {
        // Given
        doThrow(IllegalArgumentException("Body is not a readable PDF")).whenever(pdfToMarkdownService).convert(any(), any())

        // When / Then
        mockMvc.perform(givenRequest())
            .andExpect(status().isBadRequest)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    }

    @Test
    fun shouldRejectUnsupportedContentType() {
        // When / Then
        mockMvc.perform(givenRequest().contentType(MediaType.TEXT_PLAIN))
            .andExpect(status().isUnsupportedMediaType)
    }
}
