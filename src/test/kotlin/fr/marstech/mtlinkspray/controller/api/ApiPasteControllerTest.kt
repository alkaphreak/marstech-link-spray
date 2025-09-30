package fr.marstech.mtlinkspray.controller.api

import com.fasterxml.jackson.databind.ObjectMapper
import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.PasteEntity
import fr.marstech.mtlinkspray.enums.PastebinTextLanguageEnum
import fr.marstech.mtlinkspray.enums.PastebinTextLanguageEnum.KOTLIN
import fr.marstech.mtlinkspray.service.PasteService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@Disabled
@WebMvcTest(controllers = [PasteApiController::class, RootApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class)
class ApiPasteControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var pasteService: PasteService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun shouldCreatePasteAndReturnResponse() {
        val request = PasteRequest(
            title = "Test",
            content = "Hello World",
            language = KOTLIN.name,
            password = null,
            expiration = "1h",
            isPrivate = false,
        )
        val pasteId = "abc123"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = request.title,
            content = request.content,
            language = PastebinTextLanguageEnum.fromName(request.language)!!,
            passwordHash = null,
            isPrivate = request.isPrivate,
            isPasswordProtected = false,
            author = HistoryItem("user1")
        )
        // Use matchers for both parameters to avoid InvalidUseOfMatchersException
        `when`(pasteService.createPaste(any(), any()))
            .thenReturn(pasteId)
        `when`(pasteService.getPaste(pasteId, request.password))
            .thenReturn(pasteEntity)

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pasteId))
            .andExpect(jsonPath("$.title").value("Test"))
            .andExpect(jsonPath("$.content").value("Hello World"))
            .andExpect(jsonPath("$.language").value(KOTLIN.name))
    }

    @Test
    fun `should get paste by id`() {
        val pasteId = "abc123"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = "Test",
            content = "Hello World",
            language = KOTLIN,
            passwordHash = null,
            isPrivate = false,
            isPasswordProtected = false,
            author = HistoryItem("user1")
        )
        `when`(pasteService.getPaste(pasteId, null))
            .thenReturn(pasteEntity)

        get("/api/paste/$pasteId")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pasteId))
            .andExpect(jsonPath("$.title").value("Test"))
            .andExpect(jsonPath("$.content").value("Hello World"))
            .andExpect(jsonPath("$.language").value(KOTLIN.name))
    }

    @Test
    fun shouldUseRestExceptionHandler() {
        val request = PasteRequest(
            title = "Test",
            content = "Hello World",
            language = "kotlin",
            password = null,
            expiration = "1h",
            isPrivate = false,
        )

        // Mock the createPaste to throw exception
        `when`(pasteService.createPaste(any(), any()))
            .thenThrow(RuntimeException("Test exception"))

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Test exception"))
    }
}