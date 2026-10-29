package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.OcrResult
import java.io.InputStream

interface OcrService {
    /**
     * Recognizes the text of the PNG or JPEG image read from [image] (MLS-252). A valid
     * [rawApiKey] lifts the anonymous quota, counted per [clientIp]; a key that is present but
     * invalid throws [IllegalAccessException]. [lang] is `fra`, `eng` or both joined by `+`.
     * Throws [IllegalArgumentException] for an unsupported language or a body that is not a
     * readable PNG/JPEG, [fr.marstech.mtlinkspray.exception.PayloadTooLargeException] above the
     * configured size, and a [org.springframework.web.server.ResponseStatusException] with 429
     * when the anonymous quota is spent or 503 when the engine is busy, missing or too slow.
     */
    fun recognize(rawApiKey: String?, clientIp: String?, image: InputStream, lang: String): OcrResult
}
