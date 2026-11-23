package fr.marstech.mtlinkspray.adapter.outbound.mongo.abuse
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface AbuseReportRepository : MongoRepository<AbuseReportEntity, String>
