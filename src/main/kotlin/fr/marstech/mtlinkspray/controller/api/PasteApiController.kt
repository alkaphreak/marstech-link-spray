package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.dto.PasteResponse
import fr.marstech.mtlinkspray.dto.PasteResponse.Companion.fromEntity
import fr.marstech.mtlinkspray.service.PasteService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.nio.charset.StandardCharsets.UTF_8

@Validated
@RestController
@RequestMapping("/api/paste")
class PasteApiController(private val pasteService: PasteService) {

    @PostMapping
    fun createPaste(
        @RequestBody @Valid request: PasteRequest, httpServletRequest: HttpServletRequest
    ): ResponseEntity<PasteResponse> =
        pasteService.createPaste(request, httpServletRequest)
            .let { pasteService.getPaste(it, request.password) }
            .let { return ResponseEntity.ok(fromEntity(it)) }

    @GetMapping("/{pasteId}")
    fun getPaste(
        @PathVariable @NotBlank(message = "Paste ID cannot be blank") pasteId: String,
        @RequestParam(required = false) password: String?
    ): ResponseEntity<PasteResponse> =
        fromEntity(pasteService.getPaste(pasteId, password))
            .let { return ResponseEntity.ok(it) }

    @GetMapping("/{pasteId}/raw", produces = [TEXT_PLAIN_VALUE])
    fun getRawPaste(
        @PathVariable @NotBlank(message = "Paste ID cannot be blank") pasteId: String,
        @RequestParam(required = false) password: String?
    ): ResponseEntity<String> = try {
        pasteService.getPaste(pasteId, password).let { paste ->
            plainText(OK)
                .header(CONTENT_DISPOSITION, "inline; filename=\"paste-$pasteId.txt\"")
                .body(paste.content)
        }
    } catch (_: NoSuchElementException) {
        plainText(NOT_FOUND).body("Not found: paste $pasteId does not exist")
    } catch (_: IllegalAccessException) {
        plainText(UNAUTHORIZED).body("Unauthorized: password required or incorrect")
    }

    private fun plainText(status: HttpStatus): ResponseEntity.BodyBuilder =
        ResponseEntity.status(status)
            .contentType(
                MediaType("text", "plain", UTF_8)
            )
}