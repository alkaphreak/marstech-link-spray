package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.entity.DashboardEntity
import fr.marstech.mtlinkspray.entity.DashboardItem
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.repository.DashboardRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import java.util.Map

@Service(value = "dashboardService")
class DashboardServiceImpl(
    private val dashboardRepository: DashboardRepository
) : DashboardService {

    override fun createDashboard(dashboardDto: DashboardDto): DashboardDto = DashboardEntity(
        dashboardDto.id,
        LocalDateTime.now(),
        null,
        true,
        dashboardDto.description,
        Map.of<String, String>(),
        HistoryItem(),
        mutableListOf<HistoryItem>(),
        dashboardDto.name,
        dashboardDto.items
    ).let {
        dashboardRepository.save(it)
    }.let {
        DashboardDto(it.id, it.name, it.items, it.description)
    }

    override fun createDashboard(dashboardName: String): DashboardDto = createDashboard(
        DashboardDto(
            id = UUID.randomUUID().toString(),
            name = dashboardName,
            items = mutableListOf<DashboardItem>(),
            description = null
        )
    )

    override fun getDashboard(id: String): DashboardDto = dashboardRepository.findById(id)
        .orElseThrow()
        .let {
            DashboardDto(it.id, it.name, it.items, it.description)
        }

    override fun updateDashboard(id: String, dashboardDto: DashboardDto): DashboardDto = DashboardDto(
        id = id,
        name = dashboardDto.name,
        items = dashboardDto.items,
        description = dashboardDto.description
    )
}
