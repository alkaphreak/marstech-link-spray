package fr.marstech.mtlinkspray.service

import com.google.zxing.BinaryBitmap
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import fr.marstech.mtlinkspray.dto.QrCodeRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.imageio.ImageIO

class QrCodeServiceImplTest {

    private val qrCodeService = QrCodeServiceImpl()

    @Test
    fun shouldRenderPngThatDecodesBackToText() {
        // Given
        val text = "WIFI:T:WPA;S:Café\\;Net;P:s3cret;;"

        // When
        val png = qrCodeService.render(QrCodeRequest(text = text, size = 300))

        // Then
        val image = ImageIO.read(png.inputStream())
        assertEquals(300, image.width)
        val bitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(image)))
        assertEquals(text, QRCodeReader().decode(bitmap).text)
    }

    @Test
    fun shouldApplyColorsToPng() {
        // Given
        val request = QrCodeRequest(text = "hello", size = 128, fg = "ff0000", bg = "00ff00")

        // When
        val image = ImageIO.read(qrCodeService.render(request).inputStream())

        // Then
        val colors = (0 until image.width).flatMap { x -> (0 until image.height).map { y -> image.getRGB(x, y) and 0xffffff } }.toSet()
        assertEquals(setOf(0xff0000, 0x00ff00), colors)
    }

    @Test
    fun shouldRenderSvgWithSizeAndColors() {
        // Given
        val request = QrCodeRequest(text = "hello", size = 512, fg = "112233", bg = "ffeedd", format = "SVG")

        // When
        val svg = String(qrCodeService.render(request))

        // Then
        assertTrue(svg.startsWith("<svg"))
        assertTrue(svg.contains("width=\"512\""))
        assertTrue(svg.contains("fill=\"#112233\""))
        assertTrue(svg.contains("fill=\"#ffeedd\""))
    }

    @Test
    fun shouldRejectTextTooLongForQrCode() {
        // Given
        val request = QrCodeRequest(text = "é".repeat(2000))

        // When / Then
        assertThrows<IllegalArgumentException> { qrCodeService.render(request) }
    }
}
