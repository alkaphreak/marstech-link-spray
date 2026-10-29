package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayOutputStream
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PdfToMarkdownServiceImplTest {

    private val apiKeyService: ApiKeyService = mock()
    private val service = PdfToMarkdownServiceImpl(apiKeyService, maxInputBytes = 1024 * 1024, maxPages = 5)

    // md2pdf renders the fixtures, so the round trip covers both directions with real fonts
    private val markdownToPdf = MarkdownToPdfServiceImpl(apiKeyService, maxInputBytes = 1024 * 1024)

    private fun pdfOf(markdown: String) = markdownToPdf.render("key.secret", markdown.byteInputStream())

    private fun convert(pdf: ByteArray) = service.convert("key.secret", pdf.inputStream())

    private fun blankPdf(pages: Int, protect: Boolean = false): ByteArray = PDDocument().use { document ->
        repeat(pages) { document.addPage(PDPage()) }
        if (protect) {
            document.protect(StandardProtectionPolicy("owner", "user", AccessPermission()).apply { encryptionKeyLength = 256 })
        }
        ByteArrayOutputStream().also { document.save(it) }.toByteArray()
    }

    @Test
    fun shouldRestoreHeadingsListsAndParagraphs() {
        // Given: glyph bullets, as Word or LibreOffice export them (md2pdf draws its own as graphics)
        val pdf = pdfOf(
            """
            # Évolution du projet

            Premier paragraphe avec des accents : œuvre, Noël, ça.

            ## Étapes

            • première étape

            • seconde étape

            Dernier paragraphe.
            """.trimIndent()
        )

        // When
        val markdown = convert(pdf)

        // Then
        assertContains(markdown, "# Évolution du projet\n")
        assertContains(markdown, "## Étapes\n")
        assertContains(markdown, "- première étape\n- seconde étape\n")
        assertContains(markdown, "Premier paragraphe avec des accents : œuvre, Noël, ça.")
        assertContains(markdown, "\n\nDernier paragraphe.\n")
    }

    @Test
    fun shouldTurnStandaloneBoldLineIntoHeading() {
        // Given: a bold line at body size, as Word or the macOS license use for section titles
        val markdown = convert(pdfOf("**Contexte**\n\nTexte normal.\n\n**Gras dans** une phrase."))

        // Then
        assertEquals("# Contexte\n\nTexte normal.\n\nGras dans une phrase.\n", markdown)
    }

    @Test
    fun shouldDropPageNumberFooters() {
        // Given: md2pdf prints an "n / N" footer on every page
        val markdown = convert(pdfOf("Corps du texte."))

        // Then
        assertEquals("Corps du texte.\n", markdown)
    }

    @Test
    fun shouldJoinSoftLineBreaksInsideAParagraph() {
        // Given: long enough to wrap over several lines on an A4 page
        val sentence = "Ce paragraphe est assez long pour passer sur plusieurs lignes dans le PDF rendu. "
        val pdf = pdfOf(sentence.repeat(6))

        // When
        val markdown = convert(pdf)

        // Then
        assertEquals(sentence.repeat(6).trim(), markdown.trim())
    }

    @Test
    fun shouldReturnEmptyMarkdownWhenPdfHasNoText() {
        assertEquals("", convert(blankPdf(pages = 1)))
    }

    @Test
    fun shouldRejectInvalidApiKeyBeforeReadingBody() {
        // Given
        doThrow(IllegalAccessException("Invalid API key")).whenever(apiKeyService).resolve(any())

        // When / Then
        assertThrows<IllegalAccessException> { service.convert("bad", "not read".byteInputStream()) }
    }

    @Test
    fun shouldRejectEmptyBody() {
        assertThrows<IllegalArgumentException> { convert(ByteArray(0)) }
    }

    @Test
    fun shouldRejectBodyThatIsNotAPdf() {
        assertThrows<IllegalArgumentException> { convert("plain text".toByteArray()) }
    }

    @Test
    fun shouldRejectPasswordProtectedPdf() {
        assertThrows<IllegalArgumentException> { convert(blankPdf(pages = 1, protect = true)) }
    }

    @Test
    fun shouldRejectPdfOverMaxSize() {
        // Given
        val smallLimit = PdfToMarkdownServiceImpl(apiKeyService, maxInputBytes = 100, maxPages = 5)

        // When / Then
        assertThrows<PayloadTooLargeException> { smallLimit.convert("key.secret", ByteArray(101).inputStream()) }
    }

    @Test
    fun shouldRejectPdfOverMaxPages() {
        assertThrows<PayloadTooLargeException> { convert(blankPdf(pages = 6)) }
    }
}
