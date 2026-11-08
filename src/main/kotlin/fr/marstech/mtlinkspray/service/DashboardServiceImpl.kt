package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.entity.DashboardEntity
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.repository.DashboardRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.stereotype.Service

@Service
class DashboardServiceImpl(
    private val dashboardRepository: DashboardRepository,
    private val apiKeyService: ApiKeyService,
    private val mongoTemplate: MongoTemplate,
) : DashboardService {
    private val log = LoggerFactory.getLogger(javaClass)

    // Auto index creation is off in this app, so the slug's unique index is created explicitly (idempotent).
    // Off the startup thread: an unreachable MongoDB (test profile) must neither block nor fail startup.
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        Thread.ofVirtual().name("dashboard-slug-index").start(::ensureSlugIndex)
    }

    fun ensureSlugIndex() {
        try {
            mongoTemplate.indexOps(DashboardEntity::class.java).createIndex(Index().on("slug", ASC).unique().sparse())
        } catch (e: RuntimeException) {
            log.error("Dashboard slug unique index not created: concurrent claims of one slug can both succeed", e)
        }
    }

    override fun createDashboard(dashboardDto: DashboardDto, rawApiKey: String?): DashboardDto {
        val apiKeyId = rawApiKey?.let { apiKeyService.resolve(it).id }
        checkSlugFree(dashboardDto.slug, dashboardDto.id)
        // The id comes from the client: insert, not save, so an existing id fails with DuplicateKeyException instead of being overwritten
        return dashboardRepository.insert(
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

    // Check-then-save gives a readable error; the unique index (ensureSlugIndex) makes the loser of a concurrent claim fail
    private fun checkSlugFree(slug: String?, ownId: String) {
        if (slug != null && dashboardRepository.findBySlug(slug)?.takeIf { it.id != ownId } != null)
            throw DuplicateKeyException("Slug '$slug' is already taken")
    }

    private fun DashboardEntity.toDto() = DashboardDto(id, name, items, description, slug, bio, avatarUrl, theme)
}
