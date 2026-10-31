package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt

/** Loads [bytes] as a PDF, mapping unreadable or password-protected input to [IllegalArgumentException] (400). */
internal fun loadPdf(bytes: ByteArray): PDDocument = try {
    Loader.loadPDF(bytes)
} catch (_: InvalidPasswordException) {
    throw IllegalArgumentException("Password-protected PDFs are not supported")
} catch (_: IOException) {
    throw IllegalArgumentException("Body is not a readable PDF")
}

/**
 * PDF -> text (PDFBox) -> Markdown. Headings are inferred from font size relative to the body
 * text, bullet glyphs become `- ` items, soft line breaks inside a paragraph are joined and
 * page-number lines are dropped. Bullets drawn as graphics (not glyphs) stay plain paragraphs.
 * Tables, images and multi-column layouts come out as plain text; a scanned PDF has no text
 * layer and gives an empty result.
 */
@Service
class PdfToMarkdownServiceImpl(
    private val apiKeyService: ApiKeyService,
    @Value("\${mt.link-spray.pdf2md.max-input-bytes:10485760}") private val maxInputBytes: Int,
    @Value("\${mt.link-spray.pdf2md.max-pages:200}") private val maxPages: Int,
) : PdfToMarkdownService {

    override fun convert(rawApiKey: String?, pdf: InputStream): String {
        apiKeyService.resolve(rawApiKey)
        val bytes = pdf.readNBytes(maxInputBytes + 1)
        if (bytes.size > maxInputBytes) {
            throw PayloadTooLargeException("PDF exceeds the maximum allowed size of $maxInputBytes bytes")
        }
        require(bytes.isNotEmpty()) { "PDF body is empty" }
        return loadPdf(bytes).use { document ->
            if (document.numberOfPages > maxPages) {
                throw PayloadTooLargeException("PDF exceeds the maximum allowed page count of $maxPages")
            }
            toMarkdown(LineCollector().collect(document).filterNot { PAGE_NUMBER.matches(it.text) })
        }
    }

    private fun toMarkdown(lines: List<Line>): String {
        val bodySize = lines.groupingBy { it.size }.fold(0) { chars, line -> chars + line.text.length }
            .maxByOrNull { it.value }?.key ?: return ""
        // Biggest heading size -> #, next -> ##, anything smaller but still above the body -> ###
        val headingLevels = lines.map { it.size }.filter { it >= bodySize * HEADING_RATIO }.distinct()
            .sortedDescending().withIndex().associate { (index, size) -> size to minOf(index + 1, 3) }

        val out = StringBuilder()
        val paragraph = mutableListOf<String>()
        var inList = false
        fun flushParagraph() {
            if (paragraph.isNotEmpty()) out.append(paragraph.joinToString(" ")).append("\n\n")
            paragraph.clear()
        }
        fun endList() {
            if (inList) out.append('\n')
            inList = false
        }
        // A short all-bold line standing alone (not part of a bold paragraph) is a heading one
        // level below the size-based ones: Word, LibreOffice and the macOS license use them
        val boldLevel = minOf(headingLevels.size + 1, 3)
        fun boldJoined(first: Line?, second: Line?) =
            first?.bold == true && second != null && second.bold && !second.paragraphStart
        for ((index, line) in lines.withIndex()) {
            val bullet = BULLET.find(line.text)
            val level = headingLevels[line.size]
                ?: boldLevel.takeIf {
                    line.bold && line.text.length <= MAX_BOLD_HEADING_LENGTH && bullet == null &&
                        !boldJoined(lines.getOrNull(index - 1), line) && !boldJoined(line, lines.getOrNull(index + 1))
                }
            when {
                level != null && line.text.length <= MAX_HEADING_LENGTH -> {
                    flushParagraph(); endList()
                    out.append("#".repeat(level)).append(' ').append(line.text).append("\n\n")
                }
                // ponytail: a bullet's wrapped continuation line becomes a paragraph, indent tracking if it matters
                bullet != null -> {
                    flushParagraph()
                    out.append("- ").append(line.text.substring(bullet.range.last + 1)).append('\n')
                    inList = true
                }
                else -> {
                    endList()
                    if (line.paragraphStart) flushParagraph()
                    paragraph += line.text
                }
            }
        }
        flushParagraph()
        return out.toString().trimEnd() + "\n"
    }

    private data class Line(val text: String, val size: Float, val bold: Boolean, val paragraphStart: Boolean)

    /**
     * Collects one [Line] per text line, with its largest font size rounded to half a point and
     * whether all its glyphs are bold. A line holding only spaces marks a paragraph break.
     */
    private class LineCollector : PDFTextStripper() {
        private val lines = mutableListOf<Line>()
        private val current = StringBuilder()
        private var currentSize = 0f
        private var currentBold = true
        private var paragraphStart = true

        fun collect(document: PDDocument): List<Line> {
            getText(document)
            flushLine()
            return lines
        }

        override fun writeString(text: String, textPositions: List<TextPosition>) {
            current.append(text)
            textPositions.filter { it.unicode.isNotBlank() }.forEach {
                currentSize = maxOf(currentSize, it.fontSizeInPt)
                currentBold = currentBold && it.font.name.orEmpty().contains("Bold", ignoreCase = true)
            }
        }

        override fun writeWordSeparator() {
            current.append(' ')
        }

        override fun writeLineSeparator() = flushLine()

        override fun writeParagraphStart() {
            flushLine()
            paragraphStart = true
        }

        override fun writeParagraphEnd() = flushLine()

        private fun flushLine() {
            val text = current.toString().trim()
            if (text.isNotEmpty()) {
                lines += Line(text, (currentSize * 2).roundToInt() / 2f, currentBold, paragraphStart)
                paragraphStart = false
            } else if (current.isNotEmpty()) {
                paragraphStart = true
            }
            current.setLength(0)
            currentSize = 0f
            currentBold = true
        }
    }

    private companion object {
        const val HEADING_RATIO = 1.1f
        const val MAX_HEADING_LENGTH = 120
        const val MAX_BOLD_HEADING_LENGTH = 60
        val BULLET = Regex("^[•◦▪‣∙·–]\\s+")
        // Page footers such as "3", "3 / 12", "Page 3 of 12", "page 3 sur 12"
        val PAGE_NUMBER = Regex("^(page\\s*)?\\d+(\\s*(/|of|sur)\\s*\\d+)?$", RegexOption.IGNORE_CASE)
    }
}
