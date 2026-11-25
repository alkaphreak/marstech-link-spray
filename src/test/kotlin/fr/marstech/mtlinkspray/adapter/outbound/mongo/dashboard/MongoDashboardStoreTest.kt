package fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard

import fr.marstech.mtlinkspray.adapter.outbound.mongo.shared.HistoryItem
import fr.marstech.mtlinkspray.domain.dashboard.Dashboard
import fr.marstech.mtlinkspray.domain.dashboard.DashboardLink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.IndexDefinition
import org.springframework.data.mongodb.core.index.IndexOperations
import java.time.LocalDateTime
import java.util.Optional

class MongoDashboardStoreTest {
    private val repository = mock<DashboardRepository>()
    private val mongoTemplate = mock<MongoTemplate>()
    private val store = MongoDashboardStore(repository, mongoTemplate)

    private val link = DashboardLink(id = "l1", name = "Site", description = "d", url = "https://example.com")

    init {
        whenever(repository.insert(any<DashboardEntity>())).thenAnswer { it.arguments[0] }
        whenever(repository.save(any<DashboardEntity>())).thenAnswer { it.arguments[0] }
    }

    @Test
    fun createShouldStoreEveryFieldAndLinkItems() {
        val dashboard = Dashboard(id = "d1", name = "Me", items = listOf(link), slug = "me", bio = "b", apiKeyId = "k")

        assertEquals(dashboard, store.create(dashboard))
        val saved = argumentCaptor<DashboardEntity>().also { verify(repository).insert(it.capture()) }.firstValue
        assertEquals(listOf(DashboardLinkEntity("l1", "Site", "d", "https://example.com")), saved.items)
        assertEquals(null, saved.author.ipAddress)
        assertEquals("k", saved.apiKeyId)
        assertEquals(false, store.create(dashboard.copy(id = "d3", isEnabled = false)).isEnabled)
    }

    @Test
    fun updateShouldKeepTheFieldsTheDomainDoesNotCarry() {
        val created = LocalDateTime.of(2026, 1, 2, 3, 4)
        val stored = DashboardEntity(
            id = "d1", creationDate = created, author = HistoryItem(ipAddress = "1.2.3.4", action = "CREATE"), metadata = mutableMapOf("m" to "v"),
            name = "Old", apiKeyId = "k",
        )
        whenever(repository.findById("d1")).thenReturn(Optional.of(stored))

        store.update(Dashboard(id = "d1", name = "New", items = listOf(link), apiKeyId = "k"))

        val saved = argumentCaptor<DashboardEntity>().also { verify(repository).save(it.capture()) }.firstValue
        assertEquals("New", saved.name)
        assertEquals(created, saved.creationDate)
        assertEquals(stored.author, saved.author)
        assertEquals(stored.metadata, saved.metadata)
        assertEquals(1, saved.items.size)
    }

    @Test
    fun findShouldMapDisabledAndUnknown() {
        whenever(repository.findBySlug("off")).thenReturn(DashboardEntity(id = "d2", author = HistoryItem(), slug = "off", isEnabled = false))
        whenever(repository.findById("nope")).thenReturn(Optional.empty())

        assertEquals(false, store.findBySlug("off")?.isEnabled)
        assertEquals(null, store.findById("nope"))
    }

    @Test
    fun shouldCreateUniqueSparseSlugIndex() {
        // Given
        val indexOps = mock<IndexOperations>()
        whenever(mongoTemplate.indexOps(DashboardEntity::class.java)).thenReturn(indexOps)
        // When
        store.ensureSlugIndex()
        // Then
        val index = argumentCaptor<IndexDefinition>().also { verify(indexOps).createIndex(it.capture()) }.firstValue
        assertEquals(1, index.indexKeys.getInteger("slug"))
        assertEquals(true, index.indexOptions.getBoolean("unique"))
        assertEquals(true, index.indexOptions.getBoolean("sparse"))
    }

    @Test
    fun shouldNotFailWhenSlugIndexCannotBeCreated() {
        // Given: MongoDB unreachable
        whenever(mongoTemplate.indexOps(DashboardEntity::class.java)).thenThrow(IllegalStateException("down"))
        // When / Then: logged, not thrown
        assertEquals(false, store.ensureSlugIndex())
    }

    @Test
    fun shouldRetrySlugIndexUntilCreatedWithCappedBackoff() {
        // Given: MongoDB down for 7 attempts, then up
        val indexOps = mock<IndexOperations>()
        whenever(mongoTemplate.indexOps(DashboardEntity::class.java)).thenReturn(indexOps)
        var calls = 0
        whenever(indexOps.createIndex(any<IndexDefinition>())).thenAnswer { if (++calls <= 7) throw IllegalStateException("down") else "slug_1" }
        val sleeps = mutableListOf<Long>()
        // When
        store.createSlugIndexWithRetry { sleeps += it.seconds }
        // Then
        assertEquals(8, calls)
        assertEquals(listOf(1L, 2L, 4L, 8L, 16L, 32L, 60L), sleeps)
    }

    @Test
    fun shouldStopSlugIndexRetryWhenInterruptedDuringSleep() {
        // Given: MongoDB down
        val indexOps = mock<IndexOperations>()
        whenever(mongoTemplate.indexOps(DashboardEntity::class.java)).thenReturn(indexOps)
        whenever(indexOps.createIndex(any<IndexDefinition>())).thenThrow(IllegalStateException("down"))
        // When: shutdown interrupts the sleep
        store.createSlugIndexWithRetry { throw InterruptedException() }
        // Then: one attempt, loop left
        verify(indexOps).createIndex(any<IndexDefinition>())
    }

    @Test
    fun shouldNotRetrySlugIndexOnceStopped() {
        // Given: context closed, retry flag set
        store.stopSlugIndexRetry()
        // When
        store.createSlugIndexWithRetry { error("must not sleep") }
        // Then
        verify(mongoTemplate, never()).indexOps(DashboardEntity::class.java)
    }

    @Test
    fun shouldStartSlugIndexCreationOnApplicationReady() {
        // Given
        val indexOps = mock<IndexOperations>()
        val created = java.util.concurrent.CountDownLatch(1)
        whenever(mongoTemplate.indexOps(DashboardEntity::class.java)).thenReturn(indexOps)
        whenever(indexOps.createIndex(any<IndexDefinition>())).thenAnswer { created.countDown(); "slug_1" }
        // When
        store.onApplicationReady()
        // Then
        assertEquals(true, created.await(5, java.util.concurrent.TimeUnit.SECONDS))
    }
}
