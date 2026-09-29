package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.ImageMetadataDto
import fr.marstech.mtlinkspray.dto.ImageUploadResponse
import fr.marstech.mtlinkspray.service.ImageService
import fr.marstech.mtlinkspray.utils.ImageTypes
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders.CACHE_CONTROL
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.core.io.InputStreamResource
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * Image storage API (MLS-203): apps authenticate with an `X-Api-Key` header
 * (`{keyId}.{secret}`). Images are streamed/proxied through MLS rather than redirected to the
 * Digital Ocean Spaces URL, so that MLS keeps full control over public/private access.
 */
@Validated
@RestController
@RequestMapping("/api/images")
class ImageApiController(private val imageService: ImageService) {

    @PostMapping
    fun uploadImage(
        @RequestHeader("X-Api-Key") apiKey: String?,
        @RequestParam file: MultipartFile,
        @RequestParam(defaultValue = "false") isPrivate: Boolean,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<ImageUploadResponse> =
        imageService.uploadImage(apiKey, file, isPrivate, httpServletRequest)
            .let { ResponseEntity.ok(ImageUploadResponse.fromEntity(it)) }

    @GetMapping
    fun listImages(@RequestHeader("X-Api-Key") apiKey: String?): ResponseEntity<List<ImageMetadataDto>> =
        imageService.listImages(apiKey)
            .map { ImageMetadataDto.fromEntity(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getImage(
        @PathVariable id: String,
        @RequestHeader("X-Api-Key", required = false) apiKey: String?,
    ): ResponseEntity<InputStreamResource> {
        val (image, stream) = imageService.downloadImage(id, apiKey)
        // Only verified raster types render inline; anything else (e.g. rows stored before the
        // upload allowlist existed) is forced to a download so it can never run under this origin.
        val isSafeImage = image.contentType in ImageTypes.ALLOWED
        val disposition = ContentDisposition.builder(if (isSafeImage) "inline" else "attachment")
            .filename(image.originalFilename)
            .build()
        return ResponseEntity.ok()
            .contentType(if (isSafeImage) MediaType.parseMediaType(image.contentType) else MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(image.sizeBytes)
            .header(CONTENT_DISPOSITION, disposition.toString())
            .header("X-Content-Type-Options", "nosniff")
            .header(CACHE_CONTROL, "max-age=86400")
            .body(InputStreamResource(stream))
    }

    @DeleteMapping("/{id}")
    fun deleteImage(
        @PathVariable id: String,
        @RequestHeader("X-Api-Key") apiKey: String?,
    ): ResponseEntity<Void> {
        imageService.deleteImage(id, apiKey)
        return ResponseEntity.noContent().build()
    }
}
