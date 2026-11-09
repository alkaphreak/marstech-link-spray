package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.DashboardDto
import fr.marstech.mtlinkspray.entity.ApiKeyEntity
import fr.marstech.mtlinkspray.entity.DashboardEntity
import fr.marstech.mtlinkspray.entity.DashboardItem
import fr.marstech.mtlinkspray.entity.DashboardLink
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.enums.DashboardTheme
import fr.marstech.mtlinkspray.repository.DashboardRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.IndexDefinition
import org.springframework.data.mongodb.core.index.IndexOperations
import java.util.*

class DashboardServiceTest {
    private val dashboardRepository = mock<DashboardRepository>()
    private val apiKeyService = mock<ApiKeyService>()
    private val mongoTemplate = mock<MongoTemplate>()
    private val dashboardService = DashboardServiceImpl(dashboardRepository, apiKeyService, mongoTemplate)

    private val ownerKey = ApiKeyEntity(id = "owner", name = "app", keyId = "k1", secretHash = "h", namespace = "ns1")
    private val otherKey = ApiKeyEntity(id = "other", name = "app2", keyId = "k2", secretHash = "h", namespace = "ns2")

    init {
        whenever(dashboardRepository.save(any<DashboardEntity>())).thenAnswer { it.arguments[0] }
        whenever(dashboardRepository.insert(any<DashboardEntity>())).thenAnswer { it.arguments[0] }
        whenever(apiKeyService.resolve("owner-raw")).thenReturn(ownerKey)
        whenever(apiKeyService.resolve("other-raw")).thenReturn(otherKey)
    }

    private fun givenStored(apiKeyId: String?, slug: String? = null, isEnabled: Boolean = true): DashboardEntity =
        DashboardEntity(id = "d1", author = HistoryItem(), name = "Old", slug = slug, apiKeyId = apiKeyId, isEnabled = isEnabled)
            .also { whenever(dashboardRepository.findById("d1")).thenReturn(Optional.of(it)) }

    @Test
    fun shouldCreateDashboardOwnedByApiKey() {
        // Given
        val items: MutableList<DashboardItem> = mutableListOf(DashboardLink(name = "Site", url = "https://example.com"))
        val dto = DashboardDto(name = "Me", items = items, slug = "me", bio = "Hi", theme = DashboardTheme.DARK)
        // When
        val created = dashboardService.createDashboard(dto, "owner-raw")
        // Then
        assertEquals(dto, created)
        val saved = argumentCaptor<DashboardEntity>().also { verify(dashboardRepository).insert(it.capture()) }.firstValue
        assertEquals("owner", saved.apiKeyId)
    }

    @Test
    fun shouldCreateAnonymousDashboardWithName() {
        // When
        val created = dashboardService.createDashboard("Test Dashboard")
        // Then
        assertEquals("Test Dashboard", created.name)
        val saved = argumentCaptor<DashboardEntity>().also { verify(dashboardRepository).insert(it.capture()) }.firstValue
        assertNull(saved.apiKeyId)
    }

    @Test
    fun shouldRejectCreationOverAnExistingId() {
        // Given
        whenever(dashboardRepository.insert(any<DashboardEntity>())).thenThrow(DuplicateKeyException("E11000 duplicate key _id"))
        // When / Then
        assertThrows<DuplicateKeyException> { dashboardService.createDashboard(DashboardDto(id = "d1"), "owner-raw") }
        verify(dashboardRepository, never()).save(any<DashboardEntity>())
    }

    @Test
    fun shouldCreateUniqueSparseSlugIndex() {
        // Given
        val indexOps = mock<IndexOperations>()
        whenever(mongoTemplate.indexOps(DashboardEntity::class.java)).thenReturn(indexOps)
        // When
        dashboardService.ensureSlugIndex()
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
        assertEquals(false, dashboardService.ensureSlugIndex())
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
        dashboardService.createSlugIndexWithRetry { sleeps += it.seconds }
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
        dashboardService.createSlugIndexWithRetry { throw InterruptedException() }
        // Then: one attempt, loop left
        verify(indexOps).createIndex(any<IndexDefinition>())
    }

    @Test
    fun shouldNotRetrySlugIndexOnceStopped() {
        // Given: context closed, retry flag set
        dashboardService.stopSlugIndexRetry()
        // When
        dashboardService.createSlugIndexWithRetry { error("must not sleep") }
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
        dashboardService.onApplicationReady()
        // Then
        assertEquals(true, created.await(5, java.util.concurrent.TimeUnit.SECONDS))
    }

    @Test
    fun shouldRejectCreationWithTakenSlug() {
        // Given
        whenever(dashboardRepository.findBySlug("me")).thenReturn(DashboardEntity(id = "x", author = HistoryItem(), slug = "me"))
        // When / Then
        assertThrows<DuplicateKeyException> { dashboardService.createDashboard(DashboardDto(slug = "me"), "owner-raw") }
    }

    @Test
    fun shouldGetDashboard() {
        // Given
        givenStored(apiKeyId = null)
        // When / Then
        assertEquals("Old", dashboardService.getDashboard("d1").name)
    }

    @Test
    fun shouldThrowWhenDashboardNotFound() {
        // Given
        whenever(dashboardRepository.findById("nope")).thenReturn(Optional.empty())
        // When / Then
        assertThrows<NoSuchElementException> { dashboardService.getDashboard("nope") }
    }

    @Test
    fun shouldGetEnabledDashboardBySlug() {
        // Given
        val stored = givenStored(apiKeyId = null, slug = "me")
        whenever(dashboardRepository.findBySlug("me")).thenReturn(stored)
        // When / Then
        assertEquals("me", dashboardService.getDashboardBySlug("me").slug)
    }

    @Test
    fun shouldHideDisabledOrUnknownSlug() {
        // Given
        val stored = givenStored(apiKeyId = null, slug = "off", isEnabled = false)
        whenever(dashboardRepository.findBySlug("off")).thenReturn(stored)
        // When / Then
        assertThrows<NoSuchElementException> { dashboardService.getDashboardBySlug("off") }
        assertThrows<NoSuchElementException> { dashboardService.getDashboardBySlug("unknown") }
    }

    @Test
    fun shouldUpdateDashboardWithOwnerKey() {
        // Given
        givenStored(apiKeyId = "owner")
        val dto = DashboardDto(id = "d1", name = "New", slug = "new", bio = "Bio", avatarUrl = "https://img/a.png")
        // When
        val updated = dashboardService.updateDashboard("d1", dto, "owner-raw")
        // Then
        assertEquals(dto, updated)
    }

    @Test
    fun shouldKeepOwnSlugOnUpdate() {
        // Given
        val stored = givenStored(apiKeyId = "owner", slug = "me")
        whenever(dashboardRepository.findBySlug("me")).thenReturn(stored)
        // When / Then
        assertEquals("me", dashboardService.updateDashboard("d1", DashboardDto(id = "d1", slug = "me"), "owner-raw").slug)
    }

    @Test
    fun shouldRejectUpdateWithAnotherKey() {
        // Given
        givenStored(apiKeyId = "owner")
        // When / Then
        assertThrows<IllegalAccessException> { dashboardService.updateDashboard("d1", DashboardDto(id = "d1"), "other-raw") }
        verify(dashboardRepository, never()).save(any<DashboardEntity>())
    }

    @Test
    fun shouldRejectUpdateOfAnonymousDashboard() {
        // Given
        givenStored(apiKeyId = null)
        // When / Then
        assertThrows<IllegalAccessException> { dashboardService.updateDashboard("d1", DashboardDto(id = "d1"), "owner-raw") }
    }
}
