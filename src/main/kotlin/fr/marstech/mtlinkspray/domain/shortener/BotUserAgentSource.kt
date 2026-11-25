package fr.marstech.mtlinkspray.domain.shortener

/** The vendored robot User-Agent lists (`resources/bot-user-agents`), read by an outbound adapter. */
fun interface BotUserAgentSource {

    fun load(): BotUserAgentLists
}

data class BotUserAgentLists(
    /** monperrus crawler patterns, case spelled out in the patterns themselves. */
    val crawlerPatterns: List<String>,
    /** mintlify AI agent patterns, matched case-insensitively. */
    val agentPatterns: List<String>,
    /** mintlify HTTP client patterns, matched case-insensitively. */
    val httpClients: List<HttpClientUserAgent>,
)

data class HttpClientUserAgent(val name: String, val userAgent: String)
