package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.ChecklistInstanceDto
import fr.marstech.mtlinkspray.dto.ChecklistInstanceRequest
import fr.marstech.mtlinkspray.dto.ChecklistItemStatusRequest
import fr.marstech.mtlinkspray.dto.ChecklistTemplateDto
import fr.marstech.mtlinkspray.dto.ChecklistTemplateRequest
import fr.marstech.mtlinkspray.service.ChecklistService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private const val API_KEY = "X-Api-Key"
private const val TEMPLATE_PATH = "/templates/{id}"
private const val INSTANCE_PATH = "/instances/{id}"

/** Checklists API (MLS-222, ADR 0005): templates and instances belong to the `X-Api-Key` namespace. */
@Validated
@RestController
@RequestMapping("/api/checklists")
class ChecklistApiController(private val checklistService: ChecklistService) {

    @PostMapping("/templates")
    fun createTemplate(
        @RequestHeader(API_KEY, required = false) apiKey: String?,
        @Valid @RequestBody request: ChecklistTemplateRequest,
    ): ResponseEntity<ChecklistTemplateDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(ChecklistTemplateDto.fromEntity(checklistService.createTemplate(apiKey, request)))

    @GetMapping("/templates")
    fun listTemplates(
        @RequestHeader(API_KEY, required = false) apiKey: String?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
    ): List<ChecklistTemplateDto> = checklistService.listTemplates(apiKey, page, size).map(ChecklistTemplateDto::fromEntity)

    @GetMapping(TEMPLATE_PATH)
    fun getTemplate(@RequestHeader(API_KEY, required = false) apiKey: String?, @PathVariable id: String): ChecklistTemplateDto =
        ChecklistTemplateDto.fromEntity(checklistService.getTemplate(apiKey, id))

    @PutMapping(TEMPLATE_PATH)
    fun updateTemplate(
        @RequestHeader(API_KEY, required = false) apiKey: String?,
        @PathVariable id: String,
        @Valid @RequestBody request: ChecklistTemplateRequest,
    ): ChecklistTemplateDto = ChecklistTemplateDto.fromEntity(checklistService.updateTemplate(apiKey, id, request))

    @DeleteMapping(TEMPLATE_PATH)
    fun deleteTemplate(@RequestHeader(API_KEY, required = false) apiKey: String?, @PathVariable id: String): ResponseEntity<Void> {
        checklistService.deleteTemplate(apiKey, id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/instances")
    fun createInstance(
        @RequestHeader(API_KEY, required = false) apiKey: String?,
        @Valid @RequestBody request: ChecklistInstanceRequest,
    ): ResponseEntity<ChecklistInstanceDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(ChecklistInstanceDto.fromEntity(checklistService.createInstance(apiKey, request)))

    @GetMapping("/instances")
    fun listInstances(
        @RequestHeader(API_KEY, required = false) apiKey: String?,
        @RequestParam(required = false) @Size(max = 36) templateId: String?,
        @RequestParam(required = false) @Size(max = 200) context: String?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
    ): List<ChecklistInstanceDto> =
        checklistService.listInstances(apiKey, templateId, context, page, size).map(ChecklistInstanceDto::fromEntity)

    @GetMapping(INSTANCE_PATH)
    fun getInstance(@RequestHeader(API_KEY, required = false) apiKey: String?, @PathVariable id: String): ChecklistInstanceDto =
        ChecklistInstanceDto.fromEntity(checklistService.getInstance(apiKey, id))

    @DeleteMapping(INSTANCE_PATH)
    fun deleteInstance(@RequestHeader(API_KEY, required = false) apiKey: String?, @PathVariable id: String): ResponseEntity<Void> {
        checklistService.deleteInstance(apiKey, id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/instances/{id}/items/{itemId}")
    fun updateItemStatus(
        @RequestHeader(API_KEY, required = false) apiKey: String?,
        @PathVariable id: String,
        @PathVariable itemId: String,
        @Valid @RequestBody request: ChecklistItemStatusRequest,
    ): ChecklistInstanceDto =
        ChecklistInstanceDto.fromEntity(checklistService.updateItemStatus(apiKey, id, itemId, request.status!!))
}
