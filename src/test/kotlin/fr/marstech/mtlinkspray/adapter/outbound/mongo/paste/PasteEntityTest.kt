package fr.marstech.mtlinkspray.adapter.outbound.mongo.paste

import fr.marstech.mtlinkspray.domain.paste.Paste
import fr.marstech.mtlinkspray.domain.paste.PastebinTextLanguageEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class PasteEntityTest {

    @Test
    fun shouldMapDomainPasteBothWays() {
        val paste = Paste(
            id = "id123",
            title = "Test",
            content = "Hello",
            language = PastebinTextLanguageEnum.KOTLIN,
            passwordHash = "hash",
            isPrivate = true,
            isPasswordProtected = true,
            expiresAt = LocalDateTime.now().plusHours(1),
            authorIpAddress = "203.0.113.7",
        )

        val entity = PasteEntity.of(paste)

        assertEquals("CREATE_PASTE", entity.author.action)
        assertEquals("203.0.113.7", entity.author.ipAddress)
        assertEquals(paste, entity.toDomain())
    }
}
