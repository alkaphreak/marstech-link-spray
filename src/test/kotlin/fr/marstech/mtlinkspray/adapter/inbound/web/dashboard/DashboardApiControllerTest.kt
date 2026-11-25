package fr.marstech.mtlinkspray.adapter.inbound.web.dashboard
import fr.marstech.mtlinkspray.domain.shared.ConflictException
import fr.marstech.mtlinkspray.domain.shared.ForbiddenException
import fr.marstech.mtlinkspray.domain.dashboard.Dashboard
import tools.jackson.module.kotlin.jacksonObjectMapper
import fr.marstech.mtlinkspray.adapter.inbound.web.common.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.domain.dashboard.DashboardService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class DashboardApiControllerTest {
    private lateinit var mockMvc: MockMvc
    private val dashboardService = mock<DashboardService>()
    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        val validator = org.springframework.validation.beanvalidation.LocalValidatorFactoryBean()
        validator.afterPropertiesSet()

        mockMvc = MockMvcBuilders.standaloneSetup(DashboardApiController(dashboardService))
            .setControllerAdvice(GlobalRestExceptionHandler())
            .setValidator(validator)
            .build()
    }

    @Test
    fun shouldGetDashboard() {
        val dashboard = DashboardDto()
        whenever(dashboardService.getDashboard("1")).thenReturn(dashboard.toDashboard())

        mockMvc.perform(get("/api/dashboard/1")).andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(dashboard)))
    }

    @Test
    fun shouldCreateDashboard() {
        val dashboard = DashboardDto()
        whenever(dashboardService.createDashboard(any<Dashboard>(), eq(null))).thenReturn(dashboard.toDashboard())

        mockMvc.perform(
            post("/api/dashboard").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(dashboard))
        ).andExpect(status().isCreated).andExpect(content().json(objectMapper.writeValueAsString(dashboard)))
    }

    @Test
    fun shouldUpdateDashboard() {
        val dashboard = DashboardDto()
        whenever(dashboardService.updateDashboard(eq("1"), any<Dashboard>(), eq("raw-key"))).thenReturn(dashboard.toDashboard())

        mockMvc.perform(
            put("/api/dashboard/1").header("X-Api-Key", "raw-key").contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(dashboard))
        ).andExpect(status().isOk).andExpect(content().json(objectMapper.writeValueAsString(dashboard)))
    }

    @Test
    fun shouldReturn400ForBlankId() {
        // Test with whitespace string - this should trigger our validation
        whenever(dashboardService.getDashboard(" "))
            .thenThrow(IllegalArgumentException("ID cannot be blank"))

        mockMvc.perform(get("/api/dashboard/ "))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturn404ForNonExistentDashboard() {
        whenever(dashboardService.getDashboard("nonexistent"))
            .thenThrow(NoSuchElementException("Dashboard not found"))

        mockMvc.perform(get("/api/dashboard/nonexistent"))
            .andExpect(status().isNotFound)
    }

    @Suppress("JsonStandardCompliance")
    @Test
    fun shouldReturn400ForInvalidJson() {
        mockMvc.perform(
            post("/api/dashboard")
                .contentType(APPLICATION_JSON)
                .content("\"{\"invalid json\" }\"") // Malformed JSON
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturn401WhenUpdatingWithoutOwnerKey() {
        // Given
        whenever(dashboardService.updateDashboard(eq("1"), any<Dashboard>(), eq(null)))
            .thenAnswer { throw IllegalAccessException("read-only") } // checked: thenThrow refuses it
        // When / Then
        mockMvc.perform(put("/api/dashboard/1").contentType(APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun shouldReturn403WhenUpdatingWithAnotherKey() {
        // Given
        whenever(dashboardService.updateDashboard(eq("1"), any<Dashboard>(), eq("other.key")))
            .thenThrow(ForbiddenException("Not authorized to edit this dashboard"))
        // When / Then
        mockMvc.perform(put("/api/dashboard/1").header("X-Api-Key", "other.key").contentType(APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
    }

    @Test
    fun shouldReturn409WhenSlugTaken() {
        // Given
        whenever(dashboardService.createDashboard(any<Dashboard>(), eq("raw-key")))
            .thenThrow(ConflictException("Slug 'alka' is already taken"))
        // When / Then
        mockMvc.perform(
            post("/api/dashboard").header("X-Api-Key", "raw-key").contentType(APPLICATION_JSON).content("""{"slug":"alka"}""")
        ).andExpect(status().isConflict)
    }

    @Test
    fun shouldReturn409WhenTheStoreRefusesADuplicateKey() {
        // Given: an existing id, or the loser of a race on the unique slug index
        whenever(dashboardService.createDashboard(any<Dashboard>(), eq("raw-key")))
            .thenThrow(DuplicateKeyException("E11000 duplicate key"))
        // When / Then
        mockMvc.perform(post("/api/dashboard").header("X-Api-Key", "raw-key").contentType(APPLICATION_JSON).content("{}"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("E11000 duplicate key"))
    }

    @Test
    fun shouldReturn400ForInvalidSlug() {
        mockMvc.perform(post("/api/dashboard").contentType(APPLICATION_JSON).content("""{"slug":"No Spaces!"}"""))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturn400ForNonHttpsAvatar() {
        mockMvc.perform(post("/api/dashboard").contentType(APPLICATION_JSON).content("""{"avatarUrl":"http://x.io/a.png"}"""))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturn400ForJavascriptLinkUrl() {
        mockMvc.perform(
            post("/api/dashboard").contentType(APPLICATION_JSON)
                .content("""{"items":[{"name":"x","url":"javascript:alert(1)"}]}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun shouldDeserializeLinkItems() {
        // Given
        whenever(dashboardService.createDashboard(any<Dashboard>(), eq(null))).thenAnswer { it.arguments[0] }
        // When / Then
        mockMvc.perform(
            post("/api/dashboard").contentType(APPLICATION_JSON)
                .content("""{"items":[{"name":"Site","url":"https://example.com"}]}""")
        ).andExpect(status().isCreated).andExpect(jsonPath("$.items[0].url").value("https://example.com"))
    }

    @Test
    fun shouldRejectANullItem() {
        // Jackson's Kotlin module refuses a null element in the non-null item list, as it did for the Mongo item type
        mockMvc.perform(post("/api/dashboard").contentType(APPLICATION_JSON).content("""{"items":[null]}"""))
            .andExpect(status().isBadRequest)
    }
}
