package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.QrCodeBulkRequest
import fr.marstech.mtlinkspray.dto.QrCodeRequest
import fr.marstech.mtlinkspray.service.QrCodeService
import jakarta.validation.Valid
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    /**
     * One QR code per non-blank line of the request text, zipped. POST, since the text does not fit
     * in a URL.
     */
    @PostMapping("/bulk")
    fun getQrCodeZip(@Valid request: QrCodeBulkRequest): ResponseEntity<ByteArray> {
        val lines = request.lines
        require(lines.isNotEmpty()) { "Text cannot be blank" }
        require(lines.size <= MAX_BULK_LINES) { "At most $MAX_BULK_LINES lines are accepted, got ${lines.size}" }

        val extension = request.format.lowercase()
        val zip = ByteArrayOutputStream()
        ZipOutputStream(zip).use { out ->
            lines.forEachIndexed { index, line ->
                val bytes = try {
                    qrCodeService.render(request.forLine(line))
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Line ${index + 1}: ${e.message}", e)
                }
                out.putNextEntry(ZipEntry(bulkFilename(index, line, extension)))
                out.write(bytes)
                out.closeEntry()
            }
        }
        return ResponseEntity.ok()
            .contentType(MediaType.valueOf("application/zip"))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("qrcodes.zip").build().toString()
            )
            .body(zip.toByteArray())
    }

    companion object {
        // Bounds CPU and memory: every QR code is rendered in memory before the ZIP is returned
        const val MAX_BULK_LINES = 100

        /** `001-sol4-space.png`: index keeps entries unique and ordered, the text makes them readable. */
        fun bulkFilename(index: Int, line: String, extension: String): String {
            val slug = line.replace(Regex("^[a-zA-Z]+://"), "")
                .replace(Regex("[^A-Za-z0-9]+"), "-").trim('-').take(40).trimEnd('-')
            val number = (index + 1).toString().padStart(3, '0')
            return if (slug.isEmpty()) "$number.$extension" else "$number-$slug.$extension"
        }
    }
}
