package fr.marstech.mtlinkspray.adapter.inbound.web.common
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Keeps the templates compatible with a Content-Security-Policy without 'unsafe-inline' (MLS-269):
 * page scripts live in static/assets/js/pages, styles in static/assets/css, behaviors in data-* attributes.
 */
class TemplateCspComplianceTest {

    private val forbidden = mapOf(
        "inline <script> (move it to static/assets/js/pages)" to """<script(?![^>]*\bsrc=)[^>]*>""",
        "inline event handler (use a data-* attribute handled in mt-script.js)" to """(\s|th:)on[a-z]+\s*=|th:attr=["'][^"']*\bon[a-z]+\s*=""",
        "javascript: URL" to """javascript:""",
        "<style> block (move it to static/assets/css)" to """<style\b""",
        "Bootstrap JS data-bs-* attribute (bundle removed in MLS-271, use a mt-script.js handler)" to """\bdata-bs-(?!theme(?![\w-]))""",
        "style attribute" to """(\s|th:)style(append)?\s*=|th:attr=["'][^"']*\bstyle\s*=""",
    ).mapValues { (_, pattern) -> Regex(pattern, RegexOption.IGNORE_CASE) } // HTML names are case-insensitive

    @Test
    fun shouldKeepTemplatesFreeOfInlineScriptsAndStyles() {
        // Given
        val templates = File("src/main/resources/templates").walkTopDown().filter { it.extension == "html" }.toList()

        // When
        val violations = templates.flatMap { file ->
            val html = file.readText()
            forbidden.filter { (_, regex) -> regex.containsMatchIn(html) }.keys.map { "${file.name}: $it" }
        }

        // Then
        assertTrue(templates.isNotEmpty(), "no template found")
        assertTrue(violations.isEmpty(), violations.joinToString("\n"))
    }

    @Test
    fun shouldAllowOnlyTheExactDataBsThemeAttribute() {
        // Given
        val dataBs = forbidden.entries.single { "data-bs-*" in it.key }.value

        // When
        val flagged = listOf("data-bs-theme=", "data-bs-theme-value=", "data-bs-toggle=").filter { dataBs.containsMatchIn(it) }

        // Then
        assertTrue(flagged == listOf("data-bs-theme-value=", "data-bs-toggle="), "unexpected data-bs-* verdicts: $flagged")
    }

    @Test
    fun shouldGiveEveryPageOneMainLandmarkForTheSkipLink() {
        // Given: pages that pull in the shared header, whose skip link targets #main (MLS-271)
        val pages = File("src/main/resources/templates").walkTopDown()
            .filter { it.extension == "html" && "index.html :: body-header" in it.readText() }.toList()

        // When
        val violations = pages.filter { Regex("""<main id="main">""").findAll(it.readText()).count() != 1 }.map { it.name }

        // Then
        assertTrue(pages.isNotEmpty(), "no page found")
        assertTrue(violations.isEmpty(), "pages without exactly one <main id=\"main\">: $violations")
    }
}
