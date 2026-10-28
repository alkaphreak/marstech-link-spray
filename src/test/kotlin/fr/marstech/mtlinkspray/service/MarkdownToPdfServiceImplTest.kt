package fr.marstech.mtlinkspray.service

import com.sun.net.httpserver.HttpServer
import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.InputStream
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertContains
import kotlin.test.assertEquals

class MarkdownToPdfServiceImplTest {

    private val apiKeyService: ApiKeyService = mock()
    private val service = MarkdownToPdfServiceImpl(apiKeyService, maxInputBytes = 1024)

    private fun renderText(markdown: String): String =
        Loader.loadPDF(service.render("key.secret", markdown.byteInputStream())).use { PDFTextStripper().getText(it) }

    @Test
    fun shouldRenderFootnotesTablesAndAccents() {
        // Given: footnotes made OpenHTMLtoPDF's XML parser fail before the jsoup DOM step
        val markdown = """
            # Évolution d'md2pdf

            Cœur du sujet[^1] → œuvre, Noël, ça ≤ ∞.

            | Critère | Valeur |
            |---------|--------|
            | Temps   | < 2 s  |

            [^1]: Note de bas de page.
        """.trimIndent()

        // When
        val text = renderText(markdown)

        // Then
        assertContains(text, "Évolution d'md2pdf")
        assertContains(text, "Cœur du sujet")
        assertContains(text, "→ œuvre, Noël, ça ≤ ∞")
        assertContains(text, "Note de bas de page.")
        assertContains(text, "Temps")
    }

    @Test
    fun shouldRenderTaskListMarkersAsGlyphs() {
        // Given
        val markdown = "- [x] done\n- [ ] todo"

        // When
        val text = renderText(markdown)

        // Then
        assertContains(text, "☑ done")
        assertContains(text, "☐ todo")
    }

    @Test
    fun shouldEscapeRawHtml() {
        // Given
        val markdown = "Raw: <script>alert(1)</script>"

        // When
        val text = renderText(markdown)

        // Then
        assertContains(text, "<script>alert(1)</script>")
    }

    @Test
    fun shouldNeverFetchExternalResources() {
        // Given: a local server counting every request it receives
        val hits = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/") { exchange -> hits.incrementAndGet(); exchange.sendResponseHeaders(404, -1); exchange.close() }
            start()
        }
        val url = "http://127.0.0.1:${server.address.port}"
        val markdown = "![remote]($url/image.png)\n\n<img src=\"$url/raw.png\">\n\n![local](file:///etc/hosts)"

        // When
        try {
            renderText(markdown)
        } finally {
            server.stop(0)
        }

        // Then
        assertEquals(0, hits.get())
    }

    @Test
    fun shouldRejectMarkdownOverMaxSize() {
        // Given
        val markdown = "a".repeat(1025)

        // When / Then
        assertThrows<PayloadTooLargeException> { service.render("key.secret", markdown.byteInputStream()) }
    }

    @Test
    fun shouldRejectBlankMarkdown() {
        // When / Then
        assertThrows<IllegalArgumentException> { service.render("key.secret", "  \n".byteInputStream()) }
    }

    @Test
    fun shouldCheckApiKeyBeforeReadingBody() {
        // Given
        doThrow(IllegalAccessException("Invalid API key")).whenever(apiKeyService).resolve(any())
        val body = object : InputStream() {
            override fun read(): Int = throw AssertionError("body must not be read with an invalid key")
        }

        // When / Then
        assertThrows<IllegalAccessException> { service.render("bad", body) }
    }
}
