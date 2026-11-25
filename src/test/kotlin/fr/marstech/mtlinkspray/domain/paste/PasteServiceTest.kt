package fr.marstech.mtlinkspray.domain.paste
import fr.marstech.mtlinkspray.adapter.outbound.security.BCryptPasswordHasher
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PasteServiceTest {

    private val pasteStore = mock<PasteStore>()
    private val passwordHasher = BCryptPasswordHasher()
    private val pasteService = PasteService(pasteStore, passwordHasher)

    @Test
    fun createPaste() {
        val draft = PasteDraft(
            title = "Test",
            content = "Hello",
            language = PastebinTextLanguageEnum.TEXT.name,
            password = "secret",
            expiration = ExpirationEnum.ONE_HOUR.expiration,
            isPrivate = false,
        )
        val captor = argumentCaptor<Paste>()
        whenever(pasteStore.create(captor.capture())).thenAnswer { (it.arguments[0] as Paste).copy(id = "id123") }

        val id = pasteService.createPaste(draft, ClientContext(ipAddress = "203.0.113.7"))

        assertEquals("id123", id)
        val saved = captor.firstValue
        assertEquals("Hello", saved.content)
        assertEquals("203.0.113.7", saved.authorIpAddress)
        assertTrue(saved.isPasswordProtected)
        assertTrue(passwordHasher.matches("secret", saved.passwordHash!!))
    }

    @Test
    fun createPasteWithBadExpirationLeadShouldLeadToError() {
        val draft = PasteDraft(
            title = "Test",
            content = "Hello",
            language = PastebinTextLanguageEnum.TEXT.name,
            password = "secret",
            expiration = "10m", // invalid expiration
            isPrivate = false,
        )

        assertThrows<IllegalArgumentException> {
            pasteService.createPaste(draft, ClientContext(ipAddress = null))
        }
    }

    @Test
    fun getPaste_withCorrectPassword_returnsPaste() {
        val paste = Paste(
            id = "id123",
            title = "Test",
            content = "Hello",
            language = PastebinTextLanguageEnum.TEXT,
            passwordHash = pasteService.hashPassword("secret"),
            isPasswordProtected = true,
        )
        whenever(pasteStore.findById("id123")).thenReturn(paste)

        val result = pasteService.getPaste("id123", "secret")
        assertEquals(paste, result)
    }

    @Test
    fun getPaste_withWrongPassword_throwsException() {
        val paste = Paste(
            id = "id123",
            title = "Test",
            content = "Hello",
            language = PastebinTextLanguageEnum.TEXT,
            passwordHash = pasteService.hashPassword("secret"),
            isPasswordProtected = true,
        )
        whenever(pasteStore.findById("id123")).thenReturn(paste)

        assertThrows<IllegalAccessException> {
            pasteService.getPaste("id123", "wrong")
        }
    }

    @Test
    fun getPaste_notFound_throwsException() {
        whenever(pasteStore.findById("id123")).thenReturn(null)
        assertThrows<NoSuchElementException> {
            pasteService.getPaste("id123", null)
        }
    }

    @Test
    fun deletePaste() {
        pasteService.deletePaste("id123")
        verify(pasteStore).deleteById("id123")
    }
}
