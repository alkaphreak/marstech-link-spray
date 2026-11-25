package fr.marstech.mtlinkspray.domain.shortener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Tells robots (link previews, crawlers, AI agents, HTTP libraries) from people by their User-Agent, in memory
 * only: the User-Agent is never stored (privacy page, MLS-273). Patterns are vendored, see
 * `resources/bot-user-agents/README.md`, and read through [BotUserAgentSource].
 */
@Service
class BotUserAgents(source: BotUserAgentSource) {

    // ponytail: two alternations (~1,700 patterns) scanned per redirect, measured 2026-11-18 at ~2.7 ms for a
    // Chrome User-Agent and ~12 ms at the 512-char cap; add an LRU cache of verdicts (User-Agents repeat) or an
    // Aho-Corasick prefilter on literal tokens if redirect latency or CPU ever shows it.
    // A broken pattern after a data refresh must not break redirects: matching is then off and every visit counts.
    private val patterns: List<Regex> = runCatching { compile(source.load()) }
        .onFailure { log.error("Bot User-Agent patterns not loaded: robots count as clicks", it) }
        .getOrDefault(emptyList())

    fun isBot(userAgent: String?): Boolean = isBot(userAgent, patterns)

    companion object {

        /** Bounds the regex scan: Tomcat accepts 8 KB headers, real User-Agents stay far below this. */
        const val MAX_USER_AGENT_LENGTH = 512

        private val NAMED_GROUP = Regex("""\(\?<[A-Za-z][A-Za-z0-9]*>""")
        private val log = LoggerFactory.getLogger(BotUserAgents::class.java)

        /**
         * A blank User-Agent counts as a robot (every browser sends one), unless no pattern is loaded: then nothing
         * is a robot, so a loading failure falls back to counting every visit as a click.
         */
        internal fun isBot(userAgent: String?, patterns: List<Regex>): Boolean = patterns.isNotEmpty() &&
                (userAgent.isNullOrBlank() ||
                        userAgent.take(MAX_USER_AGENT_LENGTH).let { ua -> patterns.any { it.containsMatchIn(ua) } })

        private fun compile(lists: BotUserAgentLists): List<Regex> = listOf(
            // monperrus patterns spell out their own case ([wW]get)
            alternation(lists.crawlerPatterns).toRegex(),
            // mintlify builds its patterns as case-insensitive RegExps
            alternation(agentPatterns(lists) + httpClientPatterns(lists)).toRegex(RegexOption.IGNORE_CASE),
        )

        // Each pattern in its own group, as mintlify does, so no top-level construct leaks into its neighbours
        private fun alternation(patterns: List<String>): String = patterns.joinToString("|") { "(?:$it)" }

        // Named groups like (?<version>...) repeat across entries and would clash in one alternation
        private fun agentPatterns(lists: BotUserAgentLists): List<String> =
            lists.agentPatterns.map { it.replace(NAMED_GROUP, "(?:") }

        // Generic HTTP libraries (axios, node, undici, Java...): mintlify adds them to its robot patterns. Native-app
        // stacks (CFNetwork, Dalvik) are left out: a person tapping a short link in an iOS or Android app is a click
        private fun httpClientPatterns(lists: BotUserAgentLists): List<String> = lists.httpClients
            .filterNot { it.name.contains("native app") }
            .map { it.userAgent }
    }
}
