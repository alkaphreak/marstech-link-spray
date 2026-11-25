package fr.marstech.mtlinkspray.domain.devtools

fun interface QrCodeService {

    /**
     * Render the text of [qrCode] as a QR code image.
     *
     * @return PNG bytes, or UTF-8 SVG markup when [QrCode.svg] is set
     * @throws IllegalArgumentException when the text does not fit in a QR code
     */
    fun render(qrCode: QrCode): ByteArray
}
