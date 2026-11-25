package fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shared.HistoryItem
import fr.marstech.mtlinkspray.domain.dashboard.Dashboard
import fr.marstech.mtlinkspray.domain.dashboard.DashboardItem
import fr.marstech.mtlinkspray.domain.dashboard.DashboardLink
import fr.marstech.mtlinkspray.domain.dashboard.DashboardStore
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.stereotype.Component
import java.time.Duration

/** Dashboards in MongoDB, with the unique sparse index on `slug` created at startup. */
@Component
class MongoDashboardStore(
    private val repository: DashboardRepository,
    private val mongoTemplate: MongoTemplate,
) : DashboardStore {
    private val log = LoggerFactory.getLogger(javaClass)
    private var slugIndexThread: Thread? = null

    // Own flag, not the interrupt status: a driver wait can clear the interrupt before throwing a RuntimeException
    @Volatile
    private var slugIndexRetryStopped = false

    // insert, not save: an existing id fails with DuplicateKeyException instead of being overwritten
    override fun create(dashboard: Dashboard): Dashboard = repository.insert(DashboardEntity.of(dashboard)).toDomain()

    // Read again so the fields the domain does not carry (creation date, author, history, metadata) are kept
    override fun update(dashboard: Dashboard): Dashboard = repository.save(
        repository.findById(dashboard.id).orElseThrow { NoSuchElementException("Dashboard not found") }.copy(
            name = dashboard.name,
            items = dashboard.items.mapTo(mutableListOf()) { it.toEntity() },
            description = dashboard.description,
            slug = dashboard.slug,
            bio = dashboard.bio,
            avatarUrl = dashboard.avatarUrl,
            theme = dashboard.theme,
        )
    ).toDomain()

    override fun findById(id: String): Dashboard? = repository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findBySlug(slug: String): Dashboard? = repository.findBySlug(slug)?.toDomain()

    // Auto index creation is off in this app, so the slug's unique index is created explicitly (idempotent).
    // Off the startup thread: an unreachable MongoDB (test profile) must neither block nor fail startup.
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        slugIndexThread = Thread.ofVirtual().name("dashboard-slug-index").start { createSlugIndexWithRetry() }
    }

    // A closed context (devtools restart, evicted test context) must not keep retrying against a closed MongoClient
    @PreDestroy
    fun stopSlugIndexRetry() {
        slugIndexRetryStopped = true
        slugIndexThread?.interrupt()
    }

    // Retries until MongoDB accepts the index, backing off from 1s to 60s.
    // ponytail: slug writes are not blocked while the index is missing; a duplicate slug claimed in that window
    // makes every retry fail (repeated warning), add a write guard if that ever shows up in the logs.
    fun createSlugIndexWithRetry(sleep: (Duration) -> Unit = Thread::sleep) {
        var delay = Duration.ofSeconds(1)
        var firstAttempt = true
        while (!slugIndexRetryStopped && !ensureSlugIndex(firstAttempt)) {
            firstAttempt = false
            try {
                sleep(delay)
            } catch (_: InterruptedException) {
                return
            }
            delay = minOf(delay.multipliedBy(2), Duration.ofSeconds(60))
        }
    }

    fun ensureSlugIndex(firstAttempt: Boolean = true): Boolean =
        try {
            mongoTemplate.indexOps(DashboardEntity::class.java).createIndex(Index().on("slug", ASC).unique().sparse())
            true
        } catch (e: RuntimeException) {
            if (firstAttempt) log.error("Dashboard slug unique index not created, retrying: concurrent claims of one slug can both succeed meanwhile", e)
            else log.warn("Dashboard slug unique index still not created: {}", e.toString())
            false
        }
}

fun DashboardEntity.toDomain() = Dashboard(
    id = id,
    name = name,
    // A null element (accepted by the API before ADR 0007) is skipped instead of failing the read
    items = items.filterNotNull().map { it.toDomain() },
    description = description,
    slug = slug,
    bio = bio,
    avatarUrl = avatarUrl,
    theme = theme,
    apiKeyId = apiKeyId,
    isEnabled = isEnabled,
)

/** A new dashboard document, with an empty author as the service has always stored. */
fun DashboardEntity.Companion.of(dashboard: Dashboard) = DashboardEntity(
    id = dashboard.id,
    description = dashboard.description,
    author = HistoryItem(),
    name = dashboard.name,
    items = dashboard.items.mapTo(mutableListOf()) { it.toEntity() },
    slug = dashboard.slug,
    bio = dashboard.bio,
    avatarUrl = dashboard.avatarUrl,
    theme = dashboard.theme,
    apiKeyId = dashboard.apiKeyId,
    isEnabled = dashboard.isEnabled,
)

private fun DashboardItemEntity.toDomain(): DashboardItem = when (this) {
    is DashboardLinkEntity -> DashboardLink(id, name, description, url)
    else -> error("Unknown dashboard item type ${javaClass.name}")
}

private fun DashboardItem.toEntity(): DashboardItemEntity = when (this) {
    is DashboardLink -> DashboardLinkEntity(id, name, description, url)
}
