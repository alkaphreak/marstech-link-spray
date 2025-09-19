import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.PasteEntity
import fr.marstech.mtlinkspray.repository.PasteRepository
import fr.marstech.mtlinkspray.service.PasteServiceImpl
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.*

class PasteServiceImplTest {

    private val pasteRepository = mock(PasteRepository::class.java)
    private val pasteService = PasteServiceImpl(pasteRepository)

    @Test
    fun createPaste() {
        val request = PasteRequest(
            title = "Test",
            content = "Hello",
            language = "text",
            password = "secret",
            expiration = "10m",
            isPrivate = false,
        )
        val savedPasteEntity = PasteEntity(
            id = "id123",
            title = request.title,
            content = request.content,
            language = request.language,
            passwordHash = "hashed",
            expiresAt = null,
            isPrivate = false,
            isPasswordProtected = true,
            author = HistoryItem(
                ipAddress = "user", dateTime = LocalDateTime.now()
            )
        )
        `when`(pasteRepository.save(any(PasteEntity::class.java))).thenReturn(savedPasteEntity)

        val httpServletRequest: HttpServletRequest = mock(HttpServletRequest::class.java)
        val id = pasteService.createPaste(request, httpServletRequest)
        assertEquals("id123", id)
        verify(pasteRepository).save(any(PasteEntity::class.java))
    }

    @Test
    fun getPaste_withCorrectPassword_returnsPaste() {
        val pasteEntity = PasteEntity(
            id = "id123",
            title = "Test",
            content = "Hello",
            language = "text",
            passwordHash = pasteService.hashPassword("secret"),
            isPasswordProtected = true,
            author = HistoryItem(
                ipAddress = "user", dateTime = LocalDateTime.now()
            )
        )
        `when`(pasteRepository.findById("id123")).thenReturn(Optional.of(pasteEntity))

        val result = pasteService.getPaste("id123", "secret")
        assertEquals(pasteEntity, result)
    }

    @Test
    fun getPaste_withWrongPassword_throwsException() {
        val pasteEntity = PasteEntity(
            id = "id123",
            title = "Test",
            content = "Hello",
            language = "text",
            passwordHash = pasteService.hashPassword("secret"),
            isPasswordProtected = true,
            author = HistoryItem(ipAddress = "user", dateTime = LocalDateTime.now())
        )
        `when`(pasteRepository.findById("id123")).thenReturn(Optional.of(pasteEntity))

        assertThrows<IllegalAccessException> {
            pasteService.getPaste("id123", "wrong")
        }
    }

    @Test
    fun getPaste_notFound_throwsException() {
        `when`(pasteRepository.findById("id123")).thenReturn(Optional.empty())
        assertThrows<NoSuchElementException> {
            pasteService.getPaste("id123", null)
        }
    }

    @Test
    fun deletePaste() {
        pasteService.deletePaste("id123")
        verify(pasteRepository).deleteById("id123")
    }
}