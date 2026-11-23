package fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository(value = "dashboardRepository")
interface DashboardRepository : MongoRepository<DashboardEntity, String> {
    fun findBySlug(slug: String): DashboardEntity?
}
