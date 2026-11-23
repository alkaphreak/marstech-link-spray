package fr.marstech.mtlinkspray.adapter.inbound.web.paste
import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.config.SecurityConfig
import fr.marstech.mtlinkspray.adapter.inbound.web.common.RootApiController
import fr.marstech.mtlinkspray.adapter.inbound.web.common.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.domain.paste.Paste
import fr.marstech.mtlinkspray.domain.paste.PastebinTextLanguageEnum.KOTLIN
import fr.marstech.mtlinkspray.domain.paste.PasteService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.doAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [PasteViewController::class, RootApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class PasteViewControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var pasteService: PasteService

    @MockitoBean
    lateinit var writeSignatureService: fr.marstech.mtlinkspray.domain.signature.WriteSignatureService

    @Test
    fun viewPasteLinksToQrCodeOfPasteUrl() {
        // Given
        val pasteId = "abc123"
        `when`(pasteService.isPasswordProtected(pasteId)).thenReturn(false)
        `when`(pasteService.getPaste(pasteId, null)).thenReturn(
            Paste(
                id = pasteId,
                title = "Test",
                content = "Hello",
                language = KOTLIN,
                passwordHash = null,
                isPrivate = false,
                isPasswordProtected = false
            )
        )

        // When / Then
        get("/paste/$pasteId")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/qr?text=http://localhost/paste/$pasteId")))
    }

    @Test
    fun getRawPasteReturnsPlainTextContent() {
        // Given
        val pasteId = "abc123"
        val content = "Hello, raw world!"
        val paste = Paste(
            id = pasteId,
            title = "Test",
            content = content,
            language = KOTLIN,
            passwordHash = null,
            isPrivate = false,
            isPasswordProtected = false
        )
        `when`(pasteService.getPaste(pasteId, null)).thenReturn(paste)

        // When / Then
        get("/paste/$pasteId/raw")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string(content))
            .andExpect(header().string("Content-Disposition", "inline; filename=\"paste-$pasteId.txt\""))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
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
        val paste = Paste(
            id = pasteId,
            title = "Protected",
            content = content,
            language = KOTLIN,
            passwordHash = "hash",
            isPrivate = false,
            isPasswordProtected = true
        )
        `when`(pasteService.getPaste(pasteId, password)).thenReturn(paste)

        // When / Then
        get("/paste/$pasteId/raw")
            .param("password", password)
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string(content))
    }
}
