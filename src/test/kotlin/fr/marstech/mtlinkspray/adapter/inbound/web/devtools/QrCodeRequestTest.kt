package fr.marstech.mtlinkspray.adapter.inbound.web.devtools
import fr.marstech.mtlinkspray.domain.devtools.QrCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QrCodeRequestTest {

    @Test
    fun shouldMapEveryFieldToTheDomainValue() {
        val request = QrCodeRequest(text = "hello", size = 512, fg = "112233", bg = "ffeedd", format = "SVG")

        assertEquals(QrCode("hello", 512, "112233", "ffeedd", svg = true), request.toQrCode())
    }

    @Test
    fun shouldMapPngByDefault() {
        assertEquals(QrCode("hello", 256, "000000", "ffffff", svg = false), QrCodeRequest(text = "hello").toQrCode())
    }

    @Test
    fun shouldRejectMissingText() {
        val error = assertThrows<IllegalArgumentException> { QrCodeRequest().toQrCode() }

        assertEquals("Text cannot be blank", error.message)
    }
}
