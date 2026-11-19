package fr.marstech.mtlinkspray.utils

import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import tools.jackson.databind.json.JsonMapper

/**
 * Tells robots (link previews, crawlers, AI agents, HTTP libraries) from people by their User-Agent, in memory
 * only: the User-Agent is never stored (privacy page, MLS-273). Patterns are vendored, see
 * `resources/bot-user-agents/README.md`.
 */
object BotUserAgents {

    private const val DIR = "bot-user-agents"

    /** Bounds the regex scan: Tomcat accepts 8 KB headers, real User-Agents stay far below this. */
    const val MAX_USER_AGENT_LENGTH = 512

    private val NAMED_GROUP = Regex("""\(\?<[A-Za-z][A-Za-z0-9]*>""")
    private val log = LoggerFactory.getLogger(BotUserAgents::class.java)

    // ponytail: two alternations (~1,700 patterns) scanned per redirect, measured 2026-11-18 at ~2.7 ms for a
    // Chrome User-Agent and ~12 ms at the 512-char cap; add an LRU cache of verdicts (User-Agents repeat) or an
    // Aho-Corasick prefilter on literal tokens if redirect latency or CPU ever shows it.
    // A broken pattern after a data refresh must not break redirects: matching is then off and every visit counts.
    private val patterns: List<Regex> = runCatching {
        listOf(
            // monperrus patterns spell out their own case ([wW]get)
            crawlerPatterns().joinToString("|").toRegex(),
            // mintlify builds its patterns as case-insensitive RegExps
            agentPatterns().joinToString("|").toRegex(RegexOption.IGNORE_CASE),
        )
    }.onFailure { log.error("Bot User-Agent patterns not loaded: robots count as clicks", it) }
        .getOrDefault(emptyList())

    /** A blank User-Agent counts as a robot: every browser sends one. */
    fun isBot(userAgent: String?): Boolean = userAgent.isNullOrBlank() ||
            userAgent.take(MAX_USER_AGENT_LENGTH).let { ua -> patterns.any { it.containsMatchIn(ua) } }

    private fun crawlerPatterns(): List<String> = read("crawler-user-agents.json").map { it["pattern"] as String }

    private fun agentPatterns(): List<String> = read("agent-traffic-agents.json")
        .flatMap { entry -> (entry["userAgents"] as List<*>).map { it as String } }
        // Named groups like (?<version>...) repeat across entries and would clash in one alternation
        .map { it.replace(NAMED_GROUP, "(?:") }

    @Suppress("UNCHECKED_CAST")
    private fun read(file: String): List<Map<String, Any?>> =
        ClassPathResource("$DIR/$file").inputStream.use {
            JsonMapper.builder().build().readValue(it, List::class.java) as List<Map<String, Any?>>
        }
}
