package fr.marstech.mtlinkspray.config

import fr.marstech.mtlinkspray.domain.document.MarkdownToPdfService
import fr.marstech.mtlinkspray.domain.document.PdfToMarkdownService
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Document conversion services with their limits, so the domain reads no Spring property (ADR 0007). */
@Configuration(proxyBeanMethods = false)
class DocumentConfig {

    @Bean
    fun markdownToPdfService(
        apiKeyResolver: ApiKeyResolver,
        @Value("\${mt.link-spray.md2pdf.max-input-bytes:1048576}") maxInputBytes: Int,
    ): MarkdownToPdfService = MarkdownToPdfService(apiKeyResolver, maxInputBytes)

    @Bean
    fun pdfToMarkdownService(
        apiKeyResolver: ApiKeyResolver,
        @Value("\${mt.link-spray.pdf2md.max-input-bytes:10485760}") maxInputBytes: Int,
        @Value("\${mt.link-spray.pdf2md.max-pages:200}") maxPages: Int,
    ): PdfToMarkdownService = PdfToMarkdownService(apiKeyResolver, maxInputBytes, maxPages)
}
