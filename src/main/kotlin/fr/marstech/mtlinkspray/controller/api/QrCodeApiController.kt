package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.QrCodeRequest
import fr.marstech.mtlinkspray.service.QrCodeService
import jakarta.validation.Valid
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/qr")
class QrCodeApiController(
    private val qrCodeService: QrCodeService
) {

    @GetMapping
    fun getQrCode(@Valid request: QrCodeRequest): ResponseEntity<ByteArray> {
        val (mediaType, extension) =
            if (request.isSvg) MediaType.valueOf("image/svg+xml") to "svg" else MediaType.IMAGE_PNG to "png"
        val response = ResponseEntity.ok().contentType(mediaType)
        if (request.download) {
            response.header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("qrcode.$extension").build().toString()
            )
        }
        return response.body(qrCodeService.render(request))
    }
}
