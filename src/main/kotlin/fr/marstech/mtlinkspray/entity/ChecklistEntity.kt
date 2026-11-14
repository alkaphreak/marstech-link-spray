package fr.marstech.mtlinkspray.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

/**
 * A reusable checklist (MLS-222, ADR 0005), owned by the API key namespace that created it.
 * [version] is the optimistic lock: null marks a new document, since [id] is set by default.
 * Indexes are created at startup by ChecklistService (auto-index-creation is off).
 */
@Document(collection = "checklist_templates")
data class ChecklistTemplateEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val namespace: String,
    val title: String,
    val items: List<ChecklistTemplateItem>,
    @Version val version: Long? = null,
    val creationDate: Instant = Instant.now(),
)

/** Not named `id`: Spring Data would map an embedded `id` to `_id`. */
data class ChecklistTemplateItem(
    val itemId: String = UUID.randomUUID().toString(),
    val label: String,
    val note: String? = null,
)

/** A run of a template: a copy of its items taken at creation, never resynchronised. */
@Document(collection = "checklist_instances")
data class ChecklistInstanceEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val namespace: String,
    val templateId: String,
    val templateVersion: Long,
    val title: String,
    val context: String? = null,
    val items: List<ChecklistInstanceItem>,
    val creationDate: Instant = Instant.now(),
) {
    /** DONE / (total - SKIPPED), 100 when nothing is left to do (no item, or all skipped). */
    fun progressPercent(): Int {
        val counted = items.count { it.status != ChecklistItemStatus.SKIPPED }
        if (counted == 0) return 100
        return items.count { it.status == ChecklistItemStatus.DONE } * 100 / counted
    }
}

data class ChecklistInstanceItem(
    val itemId: String,
    val label: String,
    val note: String? = null,
    val status: ChecklistItemStatus = ChecklistItemStatus.TODO,
)

enum class ChecklistItemStatus { TODO, DONE, SKIPPED }
