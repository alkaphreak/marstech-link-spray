package fr.marstech.mtlinkspray.controller

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Keeps the templates compatible with a Content-Security-Policy without 'unsafe-inline' (MLS-269):
 * page scripts live in static/assets/js/pages, styles in static/assets/css, behaviors in data-* attributes.
 */
class TemplateCspComplianceTest {

    private val forbidden = mapOf(
        "inline <script> (move it to static/assets/js/pages)" to Regex("""<script(?![^>]*\bsrc=)[^>]*>"""),
        "inline event handler (use a data-* attribute handled in mt-script.js)" to Regex("""(\s|th:)on[a-z]+\s*=|th:attr="[^"]*\bon[a-z]+\s*="""),
        "javascript: URL" to Regex("""javascript:""", RegexOption.IGNORE_CASE),
        "<style> block (move it to static/assets/css)" to Regex("""<style\b"""),
        "style attribute" to Regex("""(\s|th:)style\s*="""),
    )

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
}
