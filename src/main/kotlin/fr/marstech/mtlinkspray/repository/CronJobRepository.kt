package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.CronJobEntity
import fr.marstech.mtlinkspray.entity.CronRunEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface CronJobRepository : MongoRepository<CronJobEntity, String> {
    fun findByIsEnabledTrue(): List<CronJobEntity>
    fun findByNamespace(namespace: String): List<CronJobEntity>
    fun findByIdAndNamespace(id: String, namespace: String): Optional<CronJobEntity>
    fun countByNamespace(namespace: String): Long
}

interface CronRunRepository : MongoRepository<CronRunEntity, String> {
    fun findByJobIdOrderByStartedAtDesc(jobId: String, pageable: Pageable): List<CronRunEntity>
    fun deleteByJobId(jobId: String)
}
