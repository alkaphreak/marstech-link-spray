package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.dto.PasteResponse
import fr.marstech.mtlinkspray.dto.PasteResponse.Companion.fromEntity
import fr.marstech.mtlinkspray.service.PasteService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

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
}