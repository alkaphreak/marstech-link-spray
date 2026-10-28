package fr.marstech.mtlinkspray.service

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle
import com.openhtmltopdf.outputdevice.helper.ExternalResourceControlPriority
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import fr.marstech.mtlinkspray.exception.PayloadTooLargeException
import org.commonmark.ext.footnotes.FootnotesExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemMarker
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlRenderer
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.text.Charsets.UTF_8

/**
 * Markdown -> HTML (commonmark) -> W3C DOM (jsoup) -> PDF (OpenHTMLtoPDF), fully JVM-native.
 * Raw HTML is escaped, unsafe URL schemes are emptied and every external resource (images,
 * stylesheets, fonts) is refused, so a document can never make the server read a local file or
 * call the network.
 */
@Service
class MarkdownToPdfServiceImpl(
    private val apiKeyService: ApiKeyService,
    @Value("\${mt.link-spray.md2pdf.max-input-bytes:1048576}") private val maxInputBytes: Int,
) : MarkdownToPdfService {

    private val extensions =
        listOf(TablesExtension.create(), FootnotesExtension.create(), TaskListItemsExtension.create())
    private val parser = Parser.builder().extensions(extensions).build()

    // The first registered factory wins for a node type, so the task marker override goes
    // before the extensions.
    private val htmlRenderer = HtmlRenderer.builder()
        .nodeRendererFactory(::TaskListMarkerRenderer)
        .extensions(extensions)
        .escapeHtml(true)
        .sanitizeUrls(true)
        .build()

    private val css = ClassPathResource("md2pdf/default.css").getContentAsString(UTF_8)

    override fun render(rawApiKey: String?, markdown: InputStream): ByteArray {
        apiKeyService.resolve(rawApiKey)

        // ponytail: the size cap is the only bound on render time and page count; add an
        // executor timeout if callers ever stop being our own API-keyed apps
        val bytes = markdown.readNBytes(maxInputBytes + 1)
        if (bytes.size > maxInputBytes) {
            throw PayloadTooLargeException("Markdown exceeds the maximum allowed size of $maxInputBytes bytes")
        }
        val text = bytes.toString(UTF_8)
        if (text.isBlank()) throw IllegalArgumentException("Markdown body is empty")

        val body = htmlRenderer.render(parser.parse(text))
        val html = """<!DOCTYPE html><html><head><meta charset="UTF-8"/><style>$css</style></head><body>$body</body></html>"""

        return ByteArrayOutputStream().also { out ->
            PdfRendererBuilder().apply {
                // commonmark emits HTML5 (bare boolean attributes such as data-footnote-ref),
                // which OpenHTMLtoPDF's XML parser rejects
                withW3cDocument(W3CDom().fromJsoup(Jsoup.parse(html)), null)
                useFastMode()
                FONTS.forEach { (file, family, weight, style) ->
                    useFont({ ClassPathResource("md2pdf/fonts/$file").inputStream }, family, weight, style, true)
                }
                ExternalResourceControlPriority.entries.forEach { useExternalResourceAccessControl({ _, _ -> false }, it) }
                toStream(out)
            }.run()
        }.toByteArray()
    }

    /** OpenHTMLtoPDF ignores the checked state of an input: render the marker as a glyph. */
    private class TaskListMarkerRenderer(context: HtmlNodeRendererContext) : NodeRenderer {
        private val writer = context.writer

        override fun getNodeTypes(): Set<Class<out Node>> = setOf(TaskListItemMarker::class.java)

        override fun render(node: Node) = writer.text(if ((node as TaskListItemMarker).isChecked) "☑ " else "☐ ")
    }

    private data class EmbeddedFont(val file: String, val family: String, val weight: Int, val style: FontStyle)

    private companion object {
        val FONTS = listOf(
            EmbeddedFont("DejaVuSans.ttf", "DejaVu Sans", 400, FontStyle.NORMAL),
            EmbeddedFont("DejaVuSans-Bold.ttf", "DejaVu Sans", 700, FontStyle.NORMAL),
            EmbeddedFont("DejaVuSans-Oblique.ttf", "DejaVu Sans", 400, FontStyle.ITALIC),
            EmbeddedFont("DejaVuSansMono.ttf", "DejaVu Sans Mono", 400, FontStyle.NORMAL),
        )
    }
}
