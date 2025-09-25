package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.AbuseReportEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface AbuseReportRepository : MongoRepository<AbuseReportEntity, String>
