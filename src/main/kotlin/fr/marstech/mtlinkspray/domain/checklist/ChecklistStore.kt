package fr.marstech.mtlinkspray.domain.checklist

/** Checklist templates and instances, always read within one namespace. Pages are newest first. */
interface ChecklistStore {
    fun findTemplates(namespace: String, page: Int, size: Int): List<ChecklistTemplate>
    fun findTemplate(namespace: String, id: String): ChecklistTemplate?
    fun countTemplates(namespace: String): Long
    fun createTemplate(template: ChecklistTemplate): ChecklistTemplate

    /** Writes [template] if the stored version is still [ChecklistTemplate.version]; null when it changed or is gone. */
    fun replaceTemplate(template: ChecklistTemplate): ChecklistTemplate?
    fun deleteTemplate(id: String)

    /** [templateId] and [context] narrow the list when set. */
    fun findInstances(namespace: String, templateId: String?, context: String?, page: Int, size: Int): List<ChecklistInstance>
    fun findInstance(namespace: String, id: String): ChecklistInstance?
    fun countInstances(namespace: String): Long
    fun createInstance(instance: ChecklistInstance): ChecklistInstance
    fun deleteInstance(id: String)

    /** One atomic write of that item's status; false when the instance or the item is not found. */
    fun setItemStatus(namespace: String, id: String, itemId: String, status: ChecklistItemStatus): Boolean
}
