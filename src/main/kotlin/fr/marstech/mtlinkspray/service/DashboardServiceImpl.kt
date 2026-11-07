package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.entity.DashboardEntity
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.repository.DashboardRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

@Service
class DashboardServiceImpl(
    private val dashboardRepository: DashboardRepository,
    private val apiKeyService: ApiKeyService,
) : DashboardService {

    override fun createDashboard(dashboardDto: DashboardDto, rawApiKey: String?): DashboardDto {
        val apiKeyId = rawApiKey?.let { apiKeyService.resolve(it).id }
        // The id comes from the client: saving over an existing one would hijack it
        if (dashboardRepository.existsById(dashboardDto.id)) throw DuplicateKeyException("Dashboard id already exists")
        checkSlugFree(dashboardDto.slug, dashboardDto.id)
        return dashboardRepository.save(
            DashboardEntity(
                id = dashboardDto.id,
                description = dashboardDto.description,
                author = HistoryItem(),
                name = dashboardDto.name,
                items = dashboardDto.items,
                slug = dashboardDto.slug,
                bio = dashboardDto.bio,
                avatarUrl = dashboardDto.avatarUrl,
                theme = dashboardDto.theme,
                apiKeyId = apiKeyId,
            )
        ).toDto()
    }

    override fun createDashboard(dashboardName: String): DashboardDto =
        createDashboard(DashboardDto(name = dashboardName))

    override fun getDashboard(id: String): DashboardDto = findById(id).toDto()

    override fun getDashboardBySlug(slug: String): DashboardDto =
        dashboardRepository.findBySlug(slug)?.takeIf { it.isEnabled }?.toDto()
            ?: throw NoSuchElementException("Page not found")

    override fun updateDashboard(id: String, dashboardDto: DashboardDto, rawApiKey: String?): DashboardDto {
        val entity = findById(id)
        val ownerId = entity.apiKeyId ?: throw IllegalAccessException("Dashboard created without an API key is read-only")
        if (apiKeyService.resolve(rawApiKey).id != ownerId) throw IllegalAccessException("Not authorized to edit this dashboard")
        checkSlugFree(dashboardDto.slug, id)
        return dashboardRepository.save(
            entity.copy(
                name = dashboardDto.name,
                items = dashboardDto.items,
                description = dashboardDto.description,
                slug = dashboardDto.slug,
                bio = dashboardDto.bio,
                avatarUrl = dashboardDto.avatarUrl,
                theme = dashboardDto.theme,
            )
        ).toDto()
    }

    private fun findById(id: String): DashboardEntity =
        dashboardRepository.findById(id).orElseThrow { NoSuchElementException("Dashboard not found") }

    // ponytail: check-then-save, two concurrent claims of one slug can both pass if the unique index is missing; the index makes the loser fail
    private fun checkSlugFree(slug: String?, ownId: String) {
        if (slug != null && dashboardRepository.findBySlug(slug)?.takeIf { it.id != ownId } != null)
            throw DuplicateKeyException("Slug '$slug' is already taken")
    }

    private fun DashboardEntity.toDto() = DashboardDto(id, name, items, description, slug, bio, avatarUrl, theme)
}
