package fr.marstech.mtlinkspray.domain.devtools
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class QrCodeService {

    /**
     * Render the text of [qrCode] as a QR code image.
     *
     * @return PNG bytes, or UTF-8 SVG markup when [QrCode.svg] is set
     * @throws IllegalArgumentException when the text does not fit in a QR code
     */
    fun render(qrCode: QrCode): ByteArray =
        if (qrCode.svg) {
            toSvg(encode(qrCode.text, 0), qrCode).toByteArray()
        } else {
            toPng(encode(qrCode.text, qrCode.size), qrCode)
        }

    // size 0 gives the minimal matrix (one cell per module), which SVG scales itself
    private fun encode(text: String, size: Int): BitMatrix = try {
        QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.CHARACTER_SET to "UTF-8"))
    } catch (e: WriterException) {
        throw IllegalArgumentException("Text is too long for a QR code", e)
    }

    private fun toPng(matrix: BitMatrix, qrCode: QrCode): ByteArray {
        val fg = qrCode.fg.toInt(16)
        val bg = qrCode.bg.toInt(16)
        val image = BufferedImage(matrix.width, matrix.height, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                image.setRGB(x, y, if (matrix[x, y]) fg else bg)
            }
        }
        return ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()
    }

    private fun toSvg(matrix: BitMatrix, qrCode: QrCode): String {
        val path = buildString {
            for (y in 0 until matrix.height) {
                for (x in 0 until matrix.width) {
                    if (matrix[x, y]) append("M$x ${y}h1v1h-1z")
                }
            }
        }
        val side = matrix.width
        return """<svg xmlns="http://www.w3.org/2000/svg" width="${qrCode.size}" height="${qrCode.size}" """ +
            """viewBox="0 0 $side $side" shape-rendering="crispEdges">""" +
            """<rect width="$side" height="$side" fill="#${qrCode.bg}"/>""" +
            """<path d="$path" fill="#${qrCode.fg}"/></svg>"""
    }
}
