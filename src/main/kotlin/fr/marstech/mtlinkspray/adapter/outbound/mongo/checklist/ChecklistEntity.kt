package fr.marstech.mtlinkspray.adapter.outbound.mongo.checklist

import fr.marstech.mtlinkspray.domain.checklist.ChecklistInstance
import fr.marstech.mtlinkspray.domain.checklist.ChecklistInstanceItem
import fr.marstech.mtlinkspray.domain.checklist.ChecklistItemStatus
import fr.marstech.mtlinkspray.domain.checklist.ChecklistTemplate
import fr.marstech.mtlinkspray.domain.checklist.ChecklistTemplateItem
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

/**
 * A reusable checklist (MLS-222, ADR 0005), owned by the API key namespace that created it.
 * [version] is the optimistic lock: null marks a new document, since [id] is set by default.
 * Indexes are created at startup by MongoChecklistStore (auto-index-creation is off).
 */
@Document(collection = "checklist_templates")
@TypeAlias("fr.marstech.mtlinkspray.entity.ChecklistTemplateEntity")
data class ChecklistTemplateEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val namespace: String,
    val title: String,
    val items: List<ChecklistTemplateItemEntity>,
    @Version val version: Long? = null,
    val creationDate: Instant = Instant.now(),
) {
    fun toDomain() = ChecklistTemplate(id, namespace, title, items.map { ChecklistTemplateItem(it.itemId, it.label, it.note) }, version, creationDate)

    companion object {
        fun of(t: ChecklistTemplate) = ChecklistTemplateEntity(
            t.id, t.namespace, t.title, t.items.map { ChecklistTemplateItemEntity(it.itemId, it.label, it.note) }, t.version, t.creationDate,
        )
    }
}

/** Not named `id`: Spring Data would map an embedded `id` to `_id`. */
data class ChecklistTemplateItemEntity(
    val itemId: String = UUID.randomUUID().toString(),
    val label: String,
    val note: String? = null,
)

/** A run of a template: a copy of its items taken at creation, never resynchronised. */
@Document(collection = "checklist_instances")
@TypeAlias("fr.marstech.mtlinkspray.entity.ChecklistInstanceEntity")
data class ChecklistInstanceEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val namespace: String,
    val templateId: String,
    val templateVersion: Long,
    val title: String,
    val context: String? = null,
    val items: List<ChecklistInstanceItemEntity>,
    val creationDate: Instant = Instant.now(),
) {
    fun toDomain() = ChecklistInstance(
        id, namespace, templateId, templateVersion, title, context,
        items.map { ChecklistInstanceItem(it.itemId, it.label, it.note, it.status) }, creationDate,
    )

    companion object {
        fun of(i: ChecklistInstance) = ChecklistInstanceEntity(
            i.id, i.namespace, i.templateId, i.templateVersion, i.title, i.context,
            i.items.map { ChecklistInstanceItemEntity(it.itemId, it.label, it.note, it.status) }, i.creationDate,
        )
    }
}

data class ChecklistInstanceItemEntity(
    val itemId: String,
    val label: String,
    val note: String? = null,
    val status: ChecklistItemStatus = ChecklistItemStatus.TODO,
)
