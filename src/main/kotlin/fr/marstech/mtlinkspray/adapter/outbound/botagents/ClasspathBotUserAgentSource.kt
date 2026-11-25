package fr.marstech.mtlinkspray.adapter.outbound.botagents
import fr.marstech.mtlinkspray.domain.shortener.BotUserAgentLists
import fr.marstech.mtlinkspray.domain.shortener.BotUserAgentSource
import fr.marstech.mtlinkspray.domain.shortener.HttpClientUserAgent
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

/** Reads the vendored JSON lists of `resources/bot-user-agents` (see its README for sources and refresh). */
@Component
class ClasspathBotUserAgentSource : BotUserAgentSource {

    override fun load() = BotUserAgentLists(
        crawlerPatterns = read("crawler-user-agents.json").map { it["pattern"] as String },
        agentPatterns = read("agent-traffic-agents.json")
            .flatMap { entry -> (entry["userAgents"] as List<*>).map { it as String } },
        httpClients = read("agent-traffic-http-clients.json")
            .map { HttpClientUserAgent(it["name"] as String, it["userAgent"] as String) },
    )

    @Suppress("UNCHECKED_CAST")
    private fun read(file: String): List<Map<String, Any?>> =
        ClassPathResource("$DIR/$file").inputStream.use {
            JsonMapper.builder().build().readValue(it, List::class.java) as List<Map<String, Any?>>
        }

    private companion object {
        const val DIR = "bot-user-agents"
    }
}
