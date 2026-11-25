package fr.marstech.mtlinkspray.domain.checklist

import java.time.Instant
import java.util.UUID

/**
 * A reusable checklist (MLS-222, ADR 0005), owned by the API key namespace that created it.
 * [version] is the optimistic lock: null marks a template not stored yet.
 */
data class ChecklistTemplate(
    val id: String = UUID.randomUUID().toString(),
    val namespace: String,
    val title: String,
    val items: List<ChecklistTemplateItem>,
    val version: Long? = null,
    val creationDate: Instant = Instant.now(),
)

data class ChecklistTemplateItem(
    val itemId: String = UUID.randomUUID().toString(),
    val label: String,
    val note: String? = null,
)

/** A run of a template: a copy of its items taken at creation, never resynchronised. */
data class ChecklistInstance(
    val id: String = UUID.randomUUID().toString(),
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

/** A template as the caller sends it. [version] is the one read before a replace. */
data class ChecklistTemplateDraft(val title: String, val items: List<ChecklistItemDraft>, val version: Long? = null)

/** [itemId] absent: a new item; present: an existing item of the template, kept. */
data class ChecklistItemDraft(val itemId: String? = null, val label: String, val note: String? = null)

/** Limits per namespace (`mt.link-spray.checklist.*`). */
data class ChecklistLimits(val maxTemplates: Int, val maxInstances: Int, val maxItems: Int)
