package fr.marstech.mtlinkspray.controller.api

import org.springframework.context.annotation.Import
import com.fasterxml.jackson.databind.ObjectMapper
import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.service.PasteService
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [ApiPasteController::class, ApiRootController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class)
class ApiPasteControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var pasteService: PasteService

    @MockitoBean
    lateinit var networkUtils: fr.marstech.mtlinkspray.utils.NetworkUtils

    @Autowired
    lateinit var objectMapper: ObjectMapper

    /*
        @Test
        fun `should create paste and return response`() {
            val request = PasteRequest(
                title = "Test",
                content = "Hello World",
                language = "kotlin",
                password = null,
                expiration = "1h",
                isPrivate = false,
                author = HistoryItem("user1")
            )
            val pasteId = "abc123"
            val pasteEntity = PasteEntity(
                id = pasteId,
                title = request.title,
                content = request.content,
                language = request.language,
                passwordHash = null,
                isPrivate = request.isPrivate,
                isPasswordProtected = false,
                author = HistoryItem("user1")
            )
            `when`(pasteService.createPaste(request)).thenReturn(pasteId)
            `when`(pasteService.getPaste(pasteId, request.password)).thenReturn(pasteEntity)

            post("/api/paste").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                .let(mockMvc::perform).andExpect(status().isOk).andExpect(jsonPath("$.id").value(pasteId))
                .andExpect(jsonPath("$.title").value("Test")).andExpect(jsonPath("$.content").value("Hello World"))
                .andExpect(jsonPath("$.language").value("kotlin"))
        }
    */

    /*
        @Test
        fun `should get paste by id`() {
            val pasteId = "abc123"
            val pasteEntity = PasteEntity(
                id = pasteId,
                title = "Test",
                content = "Hello World",
                language = "kotlin",
                passwordHash = null,
                isPrivate = false,
                isPasswordProtected = false,
                author = HistoryItem("user1")
            )
            `when`(pasteService.getPaste(pasteId, null)).thenReturn(pasteEntity)

            get("/api/paste/$pasteId").let(mockMvc::perform).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(pasteId)).andExpect(jsonPath("$.title").value("Test"))
                .andExpect(jsonPath("$.content").value("Hello World")).andExpect(jsonPath("$.language").value("kotlin"))
        }
    */

    @Test
    fun `should use rest exception handler`() {
        val request = PasteRequest(
            title = "Test",
            content = "Hello World",
            language = "kotlin",
            password = null,
            expiration = "1h",
            isPrivate = false,
        )

        // Mock the createPaste to throw exception
        `when`(pasteService.createPaste(any(), any<HttpServletRequest>(HttpServletRequest::class.java))).thenThrow(
            RuntimeException("Test exception")
        )

        post("/api/paste").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
            .let(mockMvc::perform).andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Test exception"))
    }
}