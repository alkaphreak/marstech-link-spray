package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.OcrResult
import fr.marstech.mtlinkspray.service.OcrService
import fr.marstech.mtlinkspray.utils.NetworkUtils
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Image or scanned PDF to text API (MLS-252, MLS-253): the request body is the raw image
 * (`curl --data-binary @scan.png -H 'Content-Type: image/png'`), like pdf2md. Open to anonymous
 * callers within a per-IP quota, which a valid `X-Api-Key` lifts (the `/ocr` page calls it anonymously).
 */
@RestController
@RequestMapping("/api/ocr")
class OcrApiController(private val ocrService: OcrService) {

    @PostMapping(consumes = [MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_PDF_VALUE])
    fun ocr(
        @RequestHeader("X-Api-Key", required = false) apiKey: String?,
        @RequestParam(defaultValue = "fra") lang: String,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<OcrResult> = ResponseEntity.ok(
        ocrService.recognize(apiKey, NetworkUtils.getIpAddress(httpServletRequest), httpServletRequest.inputStream, lang)
    )
}
