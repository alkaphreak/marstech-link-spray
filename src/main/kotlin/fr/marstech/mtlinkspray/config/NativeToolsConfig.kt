package fr.marstech.mtlinkspray.config

import fr.marstech.mtlinkspray.adapter.outbound.ratelimit.AnonymousQuota
import fr.marstech.mtlinkspray.domain.nativetools.ImageCompressionService
import fr.marstech.mtlinkspray.domain.nativetools.NativeWorkPermit
import fr.marstech.mtlinkspray.domain.nativetools.OcrService
import fr.marstech.mtlinkspray.domain.nativetools.OcrServiceImpl
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** `mt.link-spray.ocr.*`: tesseract command, input limits and anonymous quota (MLS-252, MLS-253). */
@ConfigurationProperties("mt.link-spray.ocr")
data class OcrProperties(
    val command: String = "tesseract",
    val maxInputBytes: Int = 10_485_760,
    val timeoutSeconds: Long = 30,
    val anonymousQuota: Int = 10,
    val anonymousWindowMs: Long = 3_600_000,
    val maxPdfPages: Int = 1,
    val maxPdfPixels: Long = 20_000_000,
    val maxImagePixels: Long = 12_000_000,
)

/** `mt.link-spray.img.*`: encoder commands, input and pixel limits and anonymous quota (MLS-108, ADR 0002). */
@ConfigurationProperties("mt.link-spray.img")
data class ImageCompressionProperties(
    val cwebpCommand: String = "cwebp",
    val caesiumCommand: String = "caesiumclt",
    val maxInputBytes: Int = 10_485_760,
    val timeoutSeconds: Long = 30,
    val webpMaxPixels: Long = 8_000_000,
    val jpegMaxPixels: Long = 8_000_000,
    val pngMaxPixels: Long = 3_000_000,
    val anonymousQuota: Int = 20,
    val anonymousWindowMs: Long = 3_600_000,
)

/** OCR and image compression services with their limits and quotas, so the domain reads no Spring property (ADR 0007). */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OcrProperties::class, ImageCompressionProperties::class)
class NativeToolsConfig {

    @Bean
    fun ocrService(apiKeyResolver: ApiKeyResolver, permit: NativeWorkPermit, p: OcrProperties): OcrService = OcrServiceImpl(
        apiKeyResolver, permit, AnonymousQuota("Anonymous OCR", p.anonymousQuota, p.anonymousWindowMs),
        p.command, p.maxInputBytes, p.timeoutSeconds, p.maxPdfPages, p.maxPdfPixels, p.maxImagePixels,
    )

    @Bean
    fun imageCompressionService(apiKeyResolver: ApiKeyResolver, permit: NativeWorkPermit, p: ImageCompressionProperties) =
        ImageCompressionService(
            apiKeyResolver, permit, AnonymousQuota("Anonymous image compression", p.anonymousQuota, p.anonymousWindowMs),
            p.cwebpCommand, p.caesiumCommand, p.maxInputBytes, p.timeoutSeconds, p.webpMaxPixels, p.jpegMaxPixels, p.pngMaxPixels,
        )
}
