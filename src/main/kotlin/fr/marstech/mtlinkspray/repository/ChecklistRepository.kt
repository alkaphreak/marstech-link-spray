package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.ChecklistInstanceEntity
import fr.marstech.mtlinkspray.entity.ChecklistTemplateEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Optional

interface ChecklistTemplateRepository : MongoRepository<ChecklistTemplateEntity, String> {
    fun findByNamespace(namespace: String, pageable: Pageable): List<ChecklistTemplateEntity>
    fun findByIdAndNamespace(id: String, namespace: String): Optional<ChecklistTemplateEntity>
    fun countByNamespace(namespace: String): Long
}

interface ChecklistInstanceRepository : MongoRepository<ChecklistInstanceEntity, String> {
    fun findByNamespace(namespace: String, pageable: Pageable): List<ChecklistInstanceEntity>
    fun findByNamespaceAndTemplateId(namespace: String, templateId: String, pageable: Pageable): List<ChecklistInstanceEntity>
    fun findByNamespaceAndContext(namespace: String, context: String, pageable: Pageable): List<ChecklistInstanceEntity>
    fun findByNamespaceAndTemplateIdAndContext(
        namespace: String, templateId: String, context: String, pageable: Pageable,
    ): List<ChecklistInstanceEntity>
    fun findByIdAndNamespace(id: String, namespace: String): Optional<ChecklistInstanceEntity>
    fun countByNamespace(namespace: String): Long
}
