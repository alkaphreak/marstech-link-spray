package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.ImageCompressionService
import fr.marstech.mtlinkspray.service.ImageFormat
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
 * Image compression and conversion API (MLS-108, ADR 0002): the request body is the raw PNG or JPEG
 * (`curl --data-binary @photo.jpg -H 'Content-Type: image/jpeg' '/api/img/compress?format=webp'`), the
 * response body the encoded image. `X-Image-Compressed: false` means the encoder failed or timed out and
 * the original image came back. Anonymous callers get a per-IP quota, which a valid `X-Api-Key` lifts.
 */
@RestController
@RequestMapping("/api/img")
class ImageCompressionApiController(private val imageCompressionService: ImageCompressionService) {

    @PostMapping("/compress", consumes = [MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE])
    fun compress(
        @RequestHeader("X-Api-Key", required = false) apiKey: String?,
        @RequestParam(defaultValue = "webp") format: String,
        @RequestParam(defaultValue = "75") quality: Int,
        @RequestParam(required = false) maxDimension: Int?,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<ByteArray> {
        val target = requireNotNull(ImageFormat.entries.firstOrNull { it.name.equals(format, ignoreCase = true) }) {
            "Unsupported format '$format': use webp, jpeg or png"
        }
        val result = imageCompressionService.compress(
            apiKey, NetworkUtils.getIpAddress(httpServletRequest), httpServletRequest.inputStream, target, quality, maxDimension,
        )
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(result.mediaType))
            .header("X-Image-Compressed", result.compressed.toString())
            .body(result.bytes)
    }
}
