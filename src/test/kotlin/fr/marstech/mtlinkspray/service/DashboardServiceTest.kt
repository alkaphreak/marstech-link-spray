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
import java.util.*

class DashboardServiceTest {
    private val dashboardRepository = mock<DashboardRepository>()
    private val apiKeyService = mock<ApiKeyService>()
    private val dashboardService = DashboardServiceImpl(dashboardRepository, apiKeyService)

    private val ownerKey = ApiKeyEntity(id = "owner", name = "app", keyId = "k1", secretHash = "h", namespace = "ns1")
    private val otherKey = ApiKeyEntity(id = "other", name = "app2", keyId = "k2", secretHash = "h", namespace = "ns2")

    init {
        whenever(dashboardRepository.save(any<DashboardEntity>())).thenAnswer { it.arguments[0] }
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
        val dto = DashboardDto(name = "Me", items = items, slug = "me", bio = "Hi", theme = DashboardTheme.LIGHT)
        // When
        val created = dashboardService.createDashboard(dto, "owner-raw")
        // Then
        assertEquals(dto, created)
        val saved = argumentCaptor<DashboardEntity>().also { verify(dashboardRepository).save(it.capture()) }.firstValue
        assertEquals("owner", saved.apiKeyId)
    }

    @Test
    fun shouldCreateAnonymousDashboardWithName() {
        // When
        val created = dashboardService.createDashboard("Test Dashboard")
        // Then
        assertEquals("Test Dashboard", created.name)
        val saved = argumentCaptor<DashboardEntity>().also { verify(dashboardRepository).save(it.capture()) }.firstValue
        assertNull(saved.apiKeyId)
    }

    @Test
    fun shouldRejectCreationOverAnExistingId() {
        // Given
        whenever(dashboardRepository.existsById("d1")).thenReturn(true)
        // When / Then
        assertThrows<DuplicateKeyException> { dashboardService.createDashboard(DashboardDto(id = "d1"), "owner-raw") }
        verify(dashboardRepository, never()).save(any<DashboardEntity>())
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
