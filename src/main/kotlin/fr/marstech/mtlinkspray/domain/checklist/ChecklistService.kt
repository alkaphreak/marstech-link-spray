package fr.marstech.mtlinkspray.domain.checklist
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * Checklist templates and instances (MLS-222, ADR 0005), per `X-Api-Key` namespace. An instance
 * copies its template's items at creation; editing or deleting the template never touches it.
 */
class ChecklistService(
    private val store: ChecklistStore,
    private val apiKeyResolver: ApiKeyResolver,
    private val limits: ChecklistLimits,
) {
    fun listTemplates(apiKey: String?, page: Int, size: Int): List<ChecklistTemplate> =
        store.findTemplates(namespaceOf(apiKey), page, size)

    fun getTemplate(apiKey: String?, id: String): ChecklistTemplate = getTemplateIn(namespaceOf(apiKey), id)

    fun createTemplate(apiKey: String?, draft: ChecklistTemplateDraft): ChecklistTemplate {
        val namespace = namespaceOf(apiKey)
        // ponytail: count then save is not atomic, concurrent POSTs can pass the limit by a few
        require(store.countTemplates(namespace) < limits.maxTemplates) { "Limit of ${limits.maxTemplates} checklist templates reached" }
        return store.createTemplate(ChecklistTemplate(namespace = namespace, title = draft.title, items = itemsOf(draft, emptySet())))
    }

    fun updateTemplate(apiKey: String?, id: String, draft: ChecklistTemplateDraft): ChecklistTemplate {
        val version = requireNotNull(draft.version) { "version is required to replace a template" }
        val existing = getTemplateIn(namespaceOf(apiKey), id)
        // Before the item checks: a stale edit naming an item removed since is a conflict, not a bad request.
        if (version != existing.version) throw conflict(id, version)
        val items = itemsOf(draft, existing.items.map { it.itemId }.toSet())
        // The version read by the caller, not the stored one: a concurrent edit makes the replace fail.
        return store.replaceTemplate(existing.copy(title = draft.title, items = items, version = version)) ?: throw conflict(id, version)
    }

    fun deleteTemplate(apiKey: String?, id: String) = store.deleteTemplate(getTemplate(apiKey, id).id)

    fun listInstances(apiKey: String?, templateId: String?, context: String?, page: Int, size: Int): List<ChecklistInstance> =
        store.findInstances(namespaceOf(apiKey), templateId, context?.takeIf { it.isNotBlank() }, page, size)

    fun getInstance(apiKey: String?, id: String): ChecklistInstance = getInstanceIn(namespaceOf(apiKey), id)

    fun createInstance(apiKey: String?, templateId: String, context: String?): ChecklistInstance {
        val namespace = namespaceOf(apiKey)
        require(store.countInstances(namespace) < limits.maxInstances) { "Limit of ${limits.maxInstances} checklist instances reached" }
        val template = getTemplateIn(namespace, templateId)
        return store.createInstance(
            ChecklistInstance(
                namespace = namespace, templateId = template.id, templateVersion = template.version ?: 0,
                title = template.title, context = context?.takeIf { it.isNotBlank() },
                items = template.items.map { ChecklistInstanceItem(it.itemId, it.label, it.note) },
            ),
        )
    }

    fun deleteInstance(apiKey: String?, id: String) = store.deleteInstance(getInstance(apiKey, id).id)

    /** One atomic update of the matched item: concurrent updates of two items never conflict. */
    fun updateItemStatus(apiKey: String?, id: String, itemId: String, status: ChecklistItemStatus): ChecklistInstance {
        val namespace = namespaceOf(apiKey)
        // Matched but unchanged (status already set, a retry) is a success.
        if (!store.setItemStatus(namespace, id, itemId, status)) {
            throw NoSuchElementException("Checklist item $itemId not found in instance $id")
        }
        return getInstanceIn(namespace, id)
    }

    private fun itemsOf(draft: ChecklistTemplateDraft, knownIds: Set<String>): List<ChecklistTemplateItem> {
        // Jackson lets a JSON null through into a non-null Kotlin list.
        require(draft.items.none { (it as ChecklistItemDraft?) == null }) { "items must not contain null" }
        require(draft.items.size <= limits.maxItems) { "A checklist holds at most ${limits.maxItems} items" }
        val sentIds = draft.items.mapNotNull { it.itemId }
        require(sentIds.size == sentIds.toSet().size) { "Duplicate itemId in items" }
        sentIds.firstOrNull { it !in knownIds }?.let { throw IllegalArgumentException("Unknown itemId $it") }
        return draft.items.map { item ->
            item.itemId?.let { ChecklistTemplateItem(it, item.label, item.note) } ?: ChecklistTemplateItem(label = item.label, note = item.note)
        }
    }

    private fun conflict(id: String, version: Long) =
        ResponseStatusException(HttpStatus.CONFLICT, "Template $id was modified or deleted since version $version")

    private fun getTemplateIn(namespace: String, id: String) =
        store.findTemplate(namespace, id) ?: throw NoSuchElementException("Checklist template $id not found")

    private fun getInstanceIn(namespace: String, id: String) =
        store.findInstance(namespace, id) ?: throw NoSuchElementException("Checklist instance $id not found")

    private fun namespaceOf(apiKey: String?) = apiKeyResolver.resolve(apiKey).namespace
}
