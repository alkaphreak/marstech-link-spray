package fr.marstech.mtlinkspray.controller

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Bootstrap was dropped in MLS-271: sol4.css re-implements the Bootstrap class names still in use.
 * A Bootstrap-style class added to a template or page script without a rule in the static CSS
 * would silently render unstyled, so this test fails on it.
 */
class BootstrapSubsetCoverageTest {

    private val bootstrapLike = Regex(
        """^(container|row|g[xy]?-\d|col(-\w+)*|btn(-[\w-]+)?|form-[\w-]+|input-group[\w-]*|d-[\w-]+|""" +
            """[mp][trblsexy]?-(\d|auto)|navbar[\w-]*|nav-[\w-]+|card[\w-]*|table[\w-]*|alert[\w-]*|text-[\w-]+|""" +
            """bg-[\w-]+|fw-\w+|fs-\d|lead|display-\d|list-group[\w-]*|align-[\w-]+|justify-[\w-]+|flex-[\w-]+|""" +
            """gap-\d|[wh]-\d+|rounded[\w-]*|border[\w-]*|shadow[\w-]*|visually-hidden[\w-]*|collapse|fade|show|""" +
            """spinner-[\w-]+|position-\w+|overflow-\w+|font-monospace|(in)?valid-feedback|is-(in)?valid|""" +
            """was-validated|dropdown[\w-]*|modal[\w-]*|accordion[\w-]*|progress[\w-]*|user-select-\w+|""" +
            """opacity-\d+|[vh]stack|order-\w+|float-\w+|img-fluid|figure[\w-]*|ratio[\w-]*|small|disabled|active)$"""
    )

    /** Bootstrap-looking names used only as script hooks, never styled. */
    private val hooks = setOf("btn-copy")

    @Test
    fun shouldStyleEveryBootstrapClassStillInUse() {
        // Given
        val css = File("src/main/resources/static/assets").walkTopDown()
            .filter { it.extension == "css" && "vendor" !in it.path }.joinToString("\n") { it.readText() }
        val sources = File("src/main/resources/templates").walkTopDown().filter { it.extension == "html" } +
            File("src/main/resources/static/assets/js").walkTopDown().filter { it.extension == "js" }
        val tokens = sources.flatMap { file ->
            val text = file.readText()
            (Regex("""class="([^"]*)"""").findAll(text).map { it.groupValues[1] } +
                Regex("""className\s*=\s*[`'"]([^`'"]*)""").findAll(text).map { it.groupValues[1] } +
                Regex("""th:class(?:append)?="([^"]*)"""").findAll(text)
                    .flatMap { Regex("""'([^']*)'""").findAll(it.groupValues[1]) }.map { it.groupValues[1] } +
                Regex("""classList\.(?:add|remove|toggle)\('([^']+)'""").findAll(text).map { it.groupValues[1] } +
                Regex("""el\('\w+',[^)]*'([a-z][\w -]*)'\)""").findAll(text).map { it.groupValues[1] })
                .flatMap { it.split(Regex("""\s+""")) }
        }.filter { bootstrapLike.matches(it) && it !in hooks && !it.endsWith("-") }.toSortedSet() // "alert-" + type

        // When
        val missing = tokens.filterNot { Regex("""\.${Regex.escape(it)}(?![\w-])""").containsMatchIn(css) }

        // Then
        assertTrue(tokens.isNotEmpty(), "no Bootstrap-style class found: the scan is broken")
        assertTrue(missing.isEmpty(), "Bootstrap classes without a rule in static/assets/css: $missing")
    }
}
