package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.PasteEntity
import fr.marstech.mtlinkspray.enums.PastebinTextLanguageEnum.KOTLIN
import fr.marstech.mtlinkspray.service.PasteService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.doAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [PasteViewController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class)
class PasteViewControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var pasteService: PasteService

    @Test
    fun getRawPasteReturnsPlainTextContent() {
        // Given
        val pasteId = "abc123"
        val content = "Hello, raw world!"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = "Test",
            content = content,
            language = KOTLIN,
            passwordHash = null,
            isPrivate = false,
            isPasswordProtected = false,
            author = HistoryItem("user1")
        )
        `when`(pasteService.getPaste(pasteId, null)).thenReturn(pasteEntity)

        // When / Then
        get("/paste/$pasteId/raw")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string(content))
            .andExpect(header().string("Content-Disposition", "inline; filename=\"paste-$pasteId.txt\""))
    }

    @Test
    fun getRawPasteReturnsNotFoundForNonExistentPaste() {
        // Given
        val pasteId = "nonexistent"
        `when`(pasteService.getPaste(pasteId, null))
            .thenThrow(NoSuchElementException("Paste not found"))

        // When / Then
        get("/paste/$pasteId/raw")
            .let(mockMvc::perform)
            .andExpect(status().isNotFound)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string("Not found: paste $pasteId does not exist"))
    }

    @Test
    fun getRawPasteReturnsUnauthorizedForPasswordProtectedWithoutPassword() {
        // Given
        val pasteId = "protected123"
        doAnswer { throw IllegalAccessException("Password required") }
            .`when`(pasteService).getPaste(pasteId, null)

        // When / Then
        get("/paste/$pasteId/raw")
            .let(mockMvc::perform)
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string("Unauthorized: password required or incorrect"))
    }

    @Test
    fun getRawPasteReturnsContentForPasswordProtectedWithCorrectPassword() {
        // Given
        val pasteId = "protected123"
        val password = "s3cr3t"
        val content = "Protected content here"
        val pasteEntity = PasteEntity(
            id = pasteId,
            title = "Protected",
            content = content,
            language = KOTLIN,
            passwordHash = "hash",
            isPrivate = false,
            isPasswordProtected = true,
            author = HistoryItem("user1")
        )
        `when`(pasteService.getPaste(pasteId, password)).thenReturn(pasteEntity)

        // When / Then
        get("/paste/$pasteId/raw")
            .param("password", password)
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string(content))
    }
}
