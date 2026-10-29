package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.PdfToMarkdownService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * PDF to Markdown API (MLS-196), the reverse of md2pdf: the request body is the raw PDF
 * (`curl --data-binary @doc.pdf`) and the response is the Markdown text. Authenticated with
 * `X-Api-Key` like MLS-203. No `produces` on the mapping, so error bodies can still be JSON.
 */
@RestController
@RequestMapping("/api/pdf2md")
class Pdf2MdApiController(private val pdfToMarkdownService: PdfToMarkdownService) {

    @PostMapping(consumes = [MediaType.APPLICATION_PDF_VALUE])
    fun pdf2md(
        @RequestHeader("X-Api-Key") apiKey: String?,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<String> = ResponseEntity.ok()
        .contentType(TEXT_MARKDOWN)
        .body(pdfToMarkdownService.convert(apiKey, httpServletRequest.inputStream))

    private companion object {
        val TEXT_MARKDOWN = MediaType("text", "markdown", Charsets.UTF_8)
    }
}
