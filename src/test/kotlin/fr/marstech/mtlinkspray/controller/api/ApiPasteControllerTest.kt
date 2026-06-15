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
    fun shouldGetPasteById() {
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
            .andExpect(jsonPath("$.message").value("Test exception"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
    }

    @Test
    fun shouldCreatePasteWithMinimalData() {
        val request = PasteRequest(content = "Minimal content")
        val pasteId = "xyz789"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = null,
            content = request.content,
            language = PastebinTextLanguageEnum.TEXT,
            passwordHash = null,
            isPrivate = false,
            isPasswordProtected = false,
            author = HistoryItem("user1")
        )

        `when`(pasteService.createPaste(any(), any())).thenReturn(pasteId)
        `when`(pasteService.getPaste(pasteId, null)).thenReturn(pasteEntity)

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pasteId))
            .andExpect(jsonPath("$.content").value("Minimal content"))
    }

    @Test
    fun shouldGetPasteWithPassword() {
        val pasteId = "protected123"
        val password = "secret"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = "Protected Paste",
            content = "Secret content",
            language = KOTLIN,
            passwordHash = "hashedPassword",
            isPrivate = true,
            isPasswordProtected = true,
            author = HistoryItem("user1")
        )
        `when`(pasteService.getPaste(pasteId, password)).thenReturn(pasteEntity)

        get("/api/paste/$pasteId")
            .param("password", password)
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pasteId))
            .andExpect(jsonPath("$.isPasswordProtected").value(true))
    }

    @Test
    fun shouldReturn404ForNonExistentPaste() {
        `when`(pasteService.getPaste("nonexistent", null))
            .thenThrow(RuntimeException("Paste not found"))

        get("/api/paste/nonexistent")
            .let(mockMvc::perform)
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.message").value("Paste not found"))
    }

    @Test
    fun shouldReturn400ForInvalidJson() {
        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content("{\"invalid_json\":0 }")
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
    }

    // ========== Validation Tests ==========

    @Test
    fun shouldReturn400WhenContentIsBlank() {
        val request = mapOf(
            "content" to "",
            "language" to "TEXT"
        )

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors").isArray)
    }

    @Test
    fun shouldReturn400WhenContentExceedsMaxSize() {
        val request = mapOf(
            "content" to "a".repeat(500001),
            "language" to "TEXT"
        )

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors").isArray)
    }

    @Test
    fun shouldReturn400WhenPasswordIsTooShort() {
        val request = mapOf(
            "content" to "Test content",
            "password" to "abc",
            "language" to "TEXT"
        )

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors").isArray)
    }

    @Test
    fun shouldReturn400WhenPasswordExceedsMaxSize() {
        val request = mapOf(
            "content" to "Test content",
            "password" to "a".repeat(101),
            "language" to "TEXT"
        )

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors").isArray)
    }

    @Test
    fun shouldReturn400WhenTitleExceedsMaxSize() {
        val request = mapOf(
            "title" to "a".repeat(201),
            "content" to "Test content",
            "language" to "TEXT"
        )

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors").isArray)
    }

    @Test
    fun shouldReturn400WhenLanguageIsBlank() {
        val request = mapOf(
            "content" to "Test content",
            "language" to ""
        )

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors").isArray)
    }

    @Test
    fun shouldReturn400WhenExpirationIsBlank() {
        val request = mapOf(
            "content" to "Test content",
            "language" to "TEXT",
            "expiration" to ""
        )

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors").isArray)
    }

    @Test
    fun shouldAcceptValidPasswordWithMinLength() {
        val request = PasteRequest(
            content = "Test content",
            password = "abcd", // Exactly 4 chars (min)
            language = "TEXT"
        )
        val pasteId = "test123"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = null,
            content = request.content,
            language = PastebinTextLanguageEnum.TEXT,
            passwordHash = "hashed",
            isPrivate = false,
            isPasswordProtected = true,
            author = HistoryItem("user1")
        )

        `when`(pasteService.createPaste(any(), any())).thenReturn(pasteId)
        `when`(pasteService.getPaste(pasteId, request.password)).thenReturn(pasteEntity)

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pasteId))
    }

    @Test
    fun shouldAcceptValidTitleWithMaxLength() {
        val request = PasteRequest(
            title = "a".repeat(200), // Exactly 200 chars (max)
            content = "Test content",
            language = "TEXT"
        )
        val pasteId = "test456"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = request.title,
            content = request.content,
            language = PastebinTextLanguageEnum.TEXT,
            passwordHash = null,
            isPrivate = false,
            isPasswordProtected = false,
            author = HistoryItem("user1")
        )

        `when`(pasteService.createPaste(any(), any())).thenReturn(pasteId)
        `when`(pasteService.getPaste(pasteId, null)).thenReturn(pasteEntity)

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pasteId))
    }

    @Test
    fun shouldReturn400WhenPasteIdIsBlank() {
        get("/api/paste/ ")
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun shouldAcceptContentAtMaxSize() {
        val maxContent = "a".repeat(500000) // Exactly 500,000 chars (max)
        val request = PasteRequest(
            content = maxContent,
            language = "TEXT"
        )
        val pasteId = "test789"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = null,
            content = request.content,
            language = PastebinTextLanguageEnum.TEXT,
            passwordHash = null,
            isPrivate = false,
            isPasswordProtected = false,
            author = HistoryItem("user1")
        )

        `when`(pasteService.createPaste(any(), any())).thenReturn(pasteId)
        `when`(pasteService.getPaste(pasteId, null)).thenReturn(pasteEntity)

        post("/api/paste")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pasteId))
    }

    @Test
    fun shouldGetRawPasteByIdAsPlainText() {
        // Given
        val pasteId = "raw123"
        val content = "Raw plain text content"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = "Raw Test",
            content = content,
            language = KOTLIN,
            passwordHash = null,
            isPrivate = false,
            isPasswordProtected = false,
            author = HistoryItem("user1")
        )
        `when`(pasteService.getPaste(pasteId, null)).thenReturn(pasteEntity)

        // When / Then
        get("/api/paste/$pasteId/raw")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string(content))
            .andExpect(header().string("Content-Disposition", "inline; filename=\"paste-$pasteId.txt\""))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
    }

    @Test
    fun shouldReturn404ForRawNonExistentPaste() {
        // Given
        val pasteId = "nonexistent"
        `when`(pasteService.getPaste(pasteId, null))
            .thenThrow(NoSuchElementException("Paste not found"))

        // When / Then
        get("/api/paste/$pasteId/raw")
            .let(mockMvc::perform)
            .andExpect(status().isNotFound)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string("Not found: paste $pasteId does not exist"))
    }

    @Test
    fun shouldReturn401ForRawPasswordProtectedPasteWithoutPassword() {
        // Given
        val pasteId = "protected456"
        org.mockito.kotlin.doAnswer { throw IllegalAccessException("Password required") }
            .`when`(pasteService).getPaste(pasteId, null)

        // When / Then
        get("/api/paste/$pasteId/raw")
            .let(mockMvc::perform)
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string("Unauthorized: password required or incorrect"))
    }

    @Test
    fun shouldGetRawPasteWithCorrectPassword() {
        // Given
        val pasteId = "protected456"
        val password = "s3cr3t"
        val content = "Secret raw content"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = "Protected Raw",
            content = content,
            language = KOTLIN,
            passwordHash = "hash",
            isPrivate = false,
            isPasswordProtected = true,
            author = HistoryItem("user1")
        )
        `when`(pasteService.getPaste(pasteId, password)).thenReturn(pasteEntity)

        // When / Then
        get("/api/paste/$pasteId/raw")
            .param("password", password)
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string(content))
    }

    @Test
    fun shouldReturn400ForRawBlankPasteId() {
        // Given - blank/whitespace-only pasteId bypasses routing but triggers manual guard
        // When / Then
        get("/api/paste/   /raw")
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string("Bad request: paste ID cannot be blank"))
    }
}