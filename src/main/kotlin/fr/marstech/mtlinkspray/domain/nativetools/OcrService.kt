package fr.marstech.mtlinkspray.domain.nativetools
import java.io.InputStream

interface OcrService {
    /**
     * Recognizes the text of the PNG or JPEG image, or of the PDF of at most the configured page
     * count (MLS-253), read from [image] (MLS-252). A valid
     * [rawApiKey] lifts the anonymous quota, counted per [clientIp]; a key that is present but
     * invalid throws [IllegalAccessException]. [lang] is `fra`, `eng` or both joined by `+`.
     * Throws [IllegalArgumentException] for an unsupported language or a body that is not a
     * readable PNG/JPEG/PDF, [fr.marstech.mtlinkspray.domain.shared.PayloadTooLargeException] above the
     * configured size or page count, [fr.marstech.mtlinkspray.domain.shared.QuotaExceededException] (429)
     * when the anonymous quota is spent and [fr.marstech.mtlinkspray.domain.shared.BusyException] (503) when the engine is busy, missing or too slow.
     */
    fun recognize(rawApiKey: String?, clientIp: String?, image: InputStream, lang: String): OcrResult
}
