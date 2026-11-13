package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.entity.ChecklistInstanceEntity
import fr.marstech.mtlinkspray.entity.ChecklistItemStatus
import fr.marstech.mtlinkspray.entity.ChecklistTemplateEntity
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

/** One template item. [itemId] absent: a new item; present: an existing item of that template, kept. */
data class ChecklistItemRequest(
    @field:Size(max = 36) val itemId: String? = null,
    @field:NotBlank @field:Size(max = 500) val label: String,
    @field:Size(max = 2000) val note: String? = null,
)

/**
 * Create or replace a template. On replace, [version] is the one read before the edit:
 * a stale value is refused with 409. 1000 items is a hard parsing ceiling; the configurable
 * limit (default 200) is checked by the service.
 */
data class ChecklistTemplateRequest(
    @field:NotBlank @field:Size(max = 200) val title: String,
    @field:NotEmpty @field:Size(max = 1000) @field:Valid val items: List<ChecklistItemRequest>,
    val version: Long? = null,
)

data class ChecklistInstanceRequest(
    @field:NotBlank @field:Size(max = 36) val templateId: String,
    @field:Size(max = 200) val context: String? = null,
)

data class ChecklistItemStatusRequest(@field:NotNull val status: ChecklistItemStatus?)

data class ChecklistTemplateItemDto(val itemId: String, val label: String, val note: String?)

data class ChecklistTemplateDto(
    val id: String,
    val title: String,
    val items: List<ChecklistTemplateItemDto>,
    val version: Long,
    val creationDate: Instant,
) {
    companion object {
        fun fromEntity(entity: ChecklistTemplateEntity) = ChecklistTemplateDto(
            entity.id, entity.title, entity.items.map { ChecklistTemplateItemDto(it.itemId, it.label, it.note) },
            entity.version ?: 0, entity.creationDate,
        )
    }
}

data class ChecklistInstanceItemDto(val itemId: String, val label: String, val note: String?, val status: ChecklistItemStatus)

data class ChecklistInstanceDto(
    val id: String,
    val templateId: String,
    val templateVersion: Long,
    val title: String,
    val context: String?,
    val items: List<ChecklistInstanceItemDto>,
    val progressPercent: Int,
    val creationDate: Instant,
) {
    companion object {
        fun fromEntity(entity: ChecklistInstanceEntity) = ChecklistInstanceDto(
            entity.id, entity.templateId, entity.templateVersion, entity.title, entity.context,
            entity.items.map { ChecklistInstanceItemDto(it.itemId, it.label, it.note, it.status) },
            entity.progressPercent(), entity.creationDate,
        )
    }
}
