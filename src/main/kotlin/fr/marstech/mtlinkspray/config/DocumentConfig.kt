package fr.marstech.mtlinkspray.config

import fr.marstech.mtlinkspray.domain.document.MarkdownToPdfService
import fr.marstech.mtlinkspray.domain.document.PdfToMarkdownService
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Document conversion services with their limits, so the domain reads no Spring property (ADR 0007). */
@ConfigurationProperties("mt.link-spray.md2pdf")
data class Md2PdfProperties(val maxInputBytes: Int = 1_048_576)

@ConfigurationProperties("mt.link-spray.pdf2md")
data class Pdf2MdProperties(val maxInputBytes: Int = 10_485_760, val maxPages: Int = 200)

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Md2PdfProperties::class, Pdf2MdProperties::class)
class DocumentConfig {

    @Bean
    fun markdownToPdfService(
        apiKeyResolver: ApiKeyResolver,
        p: Md2PdfProperties,
    ): MarkdownToPdfService = MarkdownToPdfService(apiKeyResolver, p.maxInputBytes)

    @Bean
    fun pdfToMarkdownService(
        apiKeyResolver: ApiKeyResolver,
        p: Pdf2MdProperties,
    ): PdfToMarkdownService = PdfToMarkdownService(apiKeyResolver, p.maxInputBytes, p.maxPages)
}
