package fr.marstech.mtlinkspray.config

import fr.marstech.mtlinkspray.adapter.outbound.ratelimit.AnonymousQuota
import fr.marstech.mtlinkspray.domain.nativetools.ImageCompressionService
import fr.marstech.mtlinkspray.domain.nativetools.NativeWorkPermit
import fr.marstech.mtlinkspray.domain.nativetools.OcrService
import fr.marstech.mtlinkspray.domain.nativetools.OcrServiceImpl
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** OCR and image compression services with their commands, limits and anonymous quotas, so the domain reads no Spring property (ADR 0007). */
@Configuration(proxyBeanMethods = false)
class NativeToolsConfig {

    @Bean
    fun ocrService(
        apiKeyResolver: ApiKeyResolver,
        permit: NativeWorkPermit,
        @Value("\${mt.link-spray.ocr.command:tesseract}") command: String,
        @Value("\${mt.link-spray.ocr.max-input-bytes:10485760}") maxInputBytes: Int,
        @Value("\${mt.link-spray.ocr.timeout-seconds:30}") timeoutSeconds: Long,
        @Value("\${mt.link-spray.ocr.anonymous-quota:10}") anonymousQuota: Int,
        @Value("\${mt.link-spray.ocr.anonymous-window-ms:3600000}") anonymousWindowMillis: Long,
        @Value("\${mt.link-spray.ocr.max-pdf-pages:1}") maxPdfPages: Int,
        @Value("\${mt.link-spray.ocr.max-pdf-pixels:20000000}") maxPdfPixels: Long,
        @Value("\${mt.link-spray.ocr.max-image-pixels:12000000}") maxImagePixels: Long,
    ): OcrService = OcrServiceImpl(
        apiKeyResolver, permit, AnonymousQuota("Anonymous OCR", anonymousQuota, anonymousWindowMillis),
        command, maxInputBytes, timeoutSeconds, maxPdfPages, maxPdfPixels, maxImagePixels,
    )

    @Bean
    fun imageCompressionService(
        apiKeyResolver: ApiKeyResolver,
        permit: NativeWorkPermit,
        @Value("\${mt.link-spray.img.cwebp-command:cwebp}") cwebp: String,
        @Value("\${mt.link-spray.img.caesium-command:caesiumclt}") caesium: String,
        @Value("\${mt.link-spray.img.max-input-bytes:10485760}") maxInputBytes: Int,
        @Value("\${mt.link-spray.img.timeout-seconds:30}") timeoutSeconds: Long,
        @Value("\${mt.link-spray.img.webp-max-pixels:8000000}") webpMaxPixels: Long,
        @Value("\${mt.link-spray.img.jpeg-max-pixels:8000000}") jpegMaxPixels: Long,
        @Value("\${mt.link-spray.img.png-max-pixels:3000000}") pngMaxPixels: Long,
        @Value("\${mt.link-spray.img.anonymous-quota:20}") anonymousQuota: Int,
        @Value("\${mt.link-spray.img.anonymous-window-ms:3600000}") anonymousWindowMillis: Long,
    ) = ImageCompressionService(
        apiKeyResolver, permit, AnonymousQuota("Anonymous image compression", anonymousQuota, anonymousWindowMillis),
        cwebp, caesium, maxInputBytes, timeoutSeconds, webpMaxPixels, jpegMaxPixels, pngMaxPixels,
    )
}
