package fr.marstech.mtlinkspray.repository

import fr.marstech.mtlinkspray.entity.DashboardEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository(value = "dashboardRepository")
interface DashboardRepository : MongoRepository<DashboardEntity, String>
