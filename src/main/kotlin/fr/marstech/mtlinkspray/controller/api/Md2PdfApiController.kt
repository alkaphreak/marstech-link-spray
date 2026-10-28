package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.MarkdownToPdfService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Markdown to PDF API (MLS-223): the request body is the raw Markdown, so an app or agent can
 * call it in one line (`curl --data-binary @doc.md`). Authenticated with `X-Api-Key` like
 * MLS-203. The body is read as a stream so its size is bounded before it is held in memory.
 */
@RestController
@RequestMapping("/api/md2pdf")
class Md2PdfApiController(private val markdownToPdfService: MarkdownToPdfService) {

    @PostMapping(consumes = ["text/markdown", MediaType.TEXT_PLAIN_VALUE])
    fun md2pdf(
        @RequestHeader("X-Api-Key") apiKey: String?,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<ByteArray> {
        val pdf = markdownToPdfService.render(apiKey, httpServletRequest.inputStream)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(CONTENT_DISPOSITION, ContentDisposition.attachment().filename("document.pdf").build().toString())
            .body(pdf)
    }
}
