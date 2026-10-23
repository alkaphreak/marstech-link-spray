package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.QrCodeRequest

fun interface QrCodeService {

    /**
     * Render the request text as a QR code image.
     *
     * @return PNG bytes, or UTF-8 SVG markup when the request format is svg
     * @throws IllegalArgumentException when the text does not fit in a QR code
     */
    fun render(request: QrCodeRequest): ByteArray
}
