package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.dto.PasteResponse
import fr.marstech.mtlinkspray.dto.PasteResponse.Companion.fromEntity
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.service.PasteService
import fr.marstech.mtlinkspray.utils.NetworkUtils
import jakarta.servlet.http.HttpServletRequest
import org.jetbrains.kotlin.it.unimi.dsi.fastutil.ints.r
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/paste")
class ApiPasteController(private val pasteService: PasteService) {

    @PostMapping
    fun createPaste(
        @RequestBody request: PasteRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<PasteResponse> {
        val pasteId = pasteService.createPaste(request, httpServletRequest)
        val pasteEntity = pasteService.getPaste(pasteId, request.password)
        val response = fromEntity(pasteEntity)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{pasteId}")
    fun getPaste(
        @PathVariable pasteId: String,
        @RequestParam(required = false) password: String?
    ): ResponseEntity<PasteResponse> =
        fromEntity(pasteService.getPaste(pasteId, password))
            .let { return ResponseEntity.ok(it) }
}