package fr.marstech.mtlinkspray.domain.dashboard
import fr.marstech.mtlinkspray.domain.shared.ConflictException
import fr.marstech.mtlinkspray.domain.shared.ForbiddenException
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.ParameterizedTest
import fr.marstech.mtlinkspray.domain.shared.ApiCaller
import fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard.DashboardEntity
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shared.HistoryItem
import fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard.DashboardRepository
import fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard.MongoDashboardStore
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
import java.util.*

class DashboardServiceTest {
    private val dashboardRepository = mock<DashboardRepository>()
    private val apiKeyResolver = mock<ApiKeyResolver>()
    private val mongoTemplate = mock<MongoTemplate>()
    private val store = MongoDashboardStore(dashboardRepository, mongoTemplate)
    private val dashboardService = DashboardServiceImpl(store, apiKeyResolver)

    private val ownerKey = ApiCaller(apiKeyId = "owner", namespace = "ns1")
    private val otherKey = ApiCaller(apiKeyId = "other", namespace = "ns2")

    init {
        whenever(dashboardRepository.save(any<DashboardEntity>())).thenAnswer { it.arguments[0] }
        whenever(dashboardRepository.insert(any<DashboardEntity>())).thenAnswer { it.arguments[0] }
        whenever(apiKeyResolver.resolve("owner-raw")).thenReturn(ownerKey)
        whenever(apiKeyResolver.resolve("other-raw")).thenReturn(otherKey)
    }

    private fun givenStored(apiKeyId: String?, slug: String? = null, isEnabled: Boolean = true): DashboardEntity =
        DashboardEntity(id = "d1", author = HistoryItem(), name = "Old", slug = slug, apiKeyId = apiKeyId, isEnabled = isEnabled)
            .also { whenever(dashboardRepository.findById("d1")).thenReturn(Optional.of(it)) }

    @Test
    fun shouldCreateDashboardOwnedByApiKey() {
        // Given
        val items: List<DashboardItem> = listOf(DashboardLink(name = "Site", url = "https://example.com"))
        val dto = Dashboard(name = "Me", items = items, slug = "me", bio = "Hi", theme = DashboardTheme.DARK)
        // When
        val created = dashboardService.createDashboard(dto, "owner-raw")
        // Then
        assertEquals(dto.copy(apiKeyId = "owner"), created)
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
        assertThrows<DuplicateKeyException> { dashboardService.createDashboard(Dashboard(id = "d1"), "owner-raw") }
        verify(dashboardRepository, never()).save(any<DashboardEntity>())
    }

    @Test
    fun shouldRejectCreationWithTakenSlug() {
        // Given
        whenever(dashboardRepository.findBySlug("me")).thenReturn(DashboardEntity(id = "x", author = HistoryItem(), slug = "me"))
        // When / Then
        assertThrows<ConflictException> { dashboardService.createDashboard(Dashboard(slug = "me"), "owner-raw") }
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
        val dto = Dashboard(id = "d1", name = "New", slug = "new", bio = "Bio", avatarUrl = "https://img/a.png")
        // When
        val updated = dashboardService.updateDashboard("d1", dto, "owner-raw")
        // Then: the content is replaced, the owner is kept
        assertEquals(dto.copy(apiKeyId = "owner"), updated)
    }

    @Test
    fun shouldKeepOwnSlugOnUpdate() {
        // Given
        val stored = givenStored(apiKeyId = "owner", slug = "me")
        whenever(dashboardRepository.findBySlug("me")).thenReturn(stored)
        // When / Then
        assertEquals("me", dashboardService.updateDashboard("d1", Dashboard(id = "d1", slug = "me"), "owner-raw").slug)
    }

    @Test
    fun shouldRejectUpdateWithAnotherKey() {
        // Given
        givenStored(apiKeyId = "owner")
        // When / Then
        assertThrows<ForbiddenException> { dashboardService.updateDashboard("d1", Dashboard(id = "d1"), "other-raw") }
        verify(dashboardRepository, never()).save(any<DashboardEntity>())
    }

    @Test
    fun shouldRejectUpdateOfAnonymousDashboard() {
        // Given
        givenStored(apiKeyId = null)
        // When / Then
        assertThrows<ForbiddenException> { dashboardService.updateDashboard("d1", Dashboard(id = "d1"), "owner-raw") }
        verify(dashboardRepository, never()).save(any<DashboardEntity>())
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["malformed", "unknown.secret"])
    fun shouldCheckCredentialsBeforeReadOnlyOnAnonymousDashboard(rawApiKey: String?) {
        // Given: credentials are checked first, a bad key is 401 even on a read-only dashboard
        givenStored(apiKeyId = null)
        whenever(apiKeyResolver.resolve(rawApiKey)).thenAnswer { throw IllegalAccessException("Invalid API key") }
        // When / Then
        assertThrows<IllegalAccessException> { dashboardService.updateDashboard("d1", Dashboard(id = "d1"), rawApiKey) }
        verify(dashboardRepository, never()).save(any<DashboardEntity>())
    }
}
