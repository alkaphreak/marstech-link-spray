package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.conf.ValidationConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.entity.ChecklistInstanceEntity
import fr.marstech.mtlinkspray.entity.ChecklistInstanceItem
import fr.marstech.mtlinkspray.entity.ChecklistItemStatus
import fr.marstech.mtlinkspray.entity.ChecklistTemplateEntity
import fr.marstech.mtlinkspray.entity.ChecklistTemplateItem
import fr.marstech.mtlinkspray.service.ChecklistService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException

@WebMvcTest(controllers = [ChecklistApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class, ValidationConfig::class)
class ChecklistApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var checklistService: ChecklistService

    private val template = ChecklistTemplateEntity(
        id = "tpl-1", namespace = "ns", title = "Trip", items = listOf(ChecklistTemplateItem("i1", "Passport")), version = 0,
    )
    private val instance = ChecklistInstanceEntity(
        id = "ins-1", namespace = "ns", templateId = "tpl-1", templateVersion = 0, title = "Trip",
        items = listOf(
            ChecklistInstanceItem("i1", "Passport", status = ChecklistItemStatus.DONE),
            ChecklistInstanceItem("i2", "Charger"),
        ),
    )
    private val templateBody = """{"title":"Trip","items":[{"label":"Passport"}]}"""
    private val json = MediaType.APPLICATION_JSON

    @Test
    fun shouldCreateTemplate() {
        // Given
        whenever(checklistService.createTemplate(eq("key"), any())).thenReturn(template)
        // When / Then
        mockMvc.perform(post("/api/checklists/templates").header("X-Api-Key", "key").contentType(json).content(templateBody))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value("tpl-1"))
            .andExpect(jsonPath("$.items[0].itemId").value("i1"))
            .andExpect(jsonPath("$.namespace").doesNotExist())
    }

    @Test
    fun shouldRejectTemplateWithoutItems() {
        mockMvc.perform(post("/api/checklists/templates").header("X-Api-Key", "key").contentType(json).content("""{"title":"Trip","items":[]}"""))
            .andExpect(status().isBadRequest)
        verify(checklistService, never()).createTemplate(any(), any())
    }

    @Test
    fun shouldRejectTooLongItemLabel() {
        val body = """{"title":"Trip","items":[{"label":"${"x".repeat(501)}"}]}"""
        mockMvc.perform(post("/api/checklists/templates").header("X-Api-Key", "key").contentType(json).content(body))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnUnauthorizedWithoutApiKey() {
        whenever(checklistService.listTemplates(null, 0, 20)).thenAnswer { throw IllegalAccessException("Missing API key") }
        mockMvc.perform(get("/api/checklists/templates")).andExpect(status().isUnauthorized)
    }

    @Test
    fun shouldRejectOversizedPage() {
        mockMvc.perform(get("/api/checklists/templates").header("X-Api-Key", "key").param("size", "101"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldGetTemplate() {
        whenever(checklistService.getTemplate("key", "tpl-1")).thenReturn(template)
        mockMvc.perform(get("/api/checklists/templates/tpl-1").header("X-Api-Key", "key"))
            .andExpect(status().isOk).andExpect(jsonPath("$.version").value(0))
    }

    @Test
    fun shouldReturnNotFoundForUnknownTemplate() {
        whenever(checklistService.getTemplate("key", "nope")).thenThrow(NoSuchElementException("not found"))
        mockMvc.perform(get("/api/checklists/templates/nope").header("X-Api-Key", "key")).andExpect(status().isNotFound)
    }

    @Test
    fun shouldReturnConflictOnStaleVersion() {
        whenever(checklistService.updateTemplate(eq("key"), eq("tpl-1"), any()))
            .thenThrow(ResponseStatusException(HttpStatus.CONFLICT, "stale"))
        mockMvc.perform(
            put("/api/checklists/templates/tpl-1").header("X-Api-Key", "key").contentType(json)
                .content("""{"title":"Trip","items":[{"label":"Passport"}],"version":0}"""),
        ).andExpect(status().isConflict)
    }

    @Test
    fun shouldDeleteTemplate() {
        mockMvc.perform(delete("/api/checklists/templates/tpl-1").header("X-Api-Key", "key")).andExpect(status().isNoContent)
        verify(checklistService).deleteTemplate("key", "tpl-1")
    }

    @Test
    fun shouldCreateInstanceWithProgress() {
        whenever(checklistService.createInstance(eq("key"), any())).thenReturn(instance)
        mockMvc.perform(
            post("/api/checklists/instances").header("X-Api-Key", "key").contentType(json).content("""{"templateId":"tpl-1","context":"Lisbon"}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.progressPercent").value(50))
            .andExpect(jsonPath("$.items[1].status").value("TODO"))
    }

    @Test
    fun shouldListInstancesWithFilters() {
        whenever(checklistService.listInstances("key", "tpl-1", null, 1, 5)).thenReturn(listOf(instance))
        mockMvc.perform(get("/api/checklists/instances").header("X-Api-Key", "key").param("templateId", "tpl-1").param("page", "1").param("size", "5"))
            .andExpect(status().isOk).andExpect(jsonPath("$[0].id").value("ins-1"))
    }

    @Test
    fun shouldGetAndDeleteInstance() {
        whenever(checklistService.getInstance("key", "ins-1")).thenReturn(instance)
        mockMvc.perform(get("/api/checklists/instances/ins-1").header("X-Api-Key", "key")).andExpect(status().isOk)
        mockMvc.perform(delete("/api/checklists/instances/ins-1").header("X-Api-Key", "key")).andExpect(status().isNoContent)
        verify(checklistService).deleteInstance("key", "ins-1")
    }

    @Test
    fun shouldUpdateItemStatus() {
        whenever(checklistService.updateItemStatus("key", "ins-1", "i2", ChecklistItemStatus.SKIPPED)).thenReturn(instance)
        mockMvc.perform(
            patch("/api/checklists/instances/ins-1/items/i2").header("X-Api-Key", "key").contentType(json).content("""{"status":"SKIPPED"}"""),
        ).andExpect(status().isOk)
    }

    @Test
    fun shouldRejectUnknownStatus() {
        mockMvc.perform(
            patch("/api/checklists/instances/ins-1/items/i2").header("X-Api-Key", "key").contentType(json).content("""{"status":"MAYBE"}"""),
        ).andExpect(status().isBadRequest)
        mockMvc.perform(
            patch("/api/checklists/instances/ins-1/items/i2").header("X-Api-Key", "key").contentType(json).content("""{}"""),
        ).andExpect(status().isBadRequest)
        verify(checklistService, never()).updateItemStatus(anyOrNull(), any(), any(), any())
    }
}
