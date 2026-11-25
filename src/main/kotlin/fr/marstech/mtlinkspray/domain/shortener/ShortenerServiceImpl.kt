package fr.marstech.mtlinkspray.domain.shortener
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ShortenerServiceImpl(
    private val shortLinkStore: ShortLinkStore,
    private val randomIdGeneratorService: RandomIdGeneratorService,
    private val botUserAgents: BotUserAgents,
) : ShortenerService {

    // Never overwrites: an ID handed out twice draws a new one instead (MLS-246).
    override fun shorten(url: String, links: LinkUrls): String? = links.shortLink(
        shortLinkStore.createWithFreeId(url, MAX_INSERT_ATTEMPTS, randomIdGeneratorService::getGeneratedFreeId).id
    )

    override fun getTarget(
        uid: String,
        userAgent: String?,
    ): String = findLink(uid).targetUrl
        .also {
            if (botUserAgents.isBot(userAgent)) shortLinkStore.countBotClick(uid)
            else shortLinkStore.countClick(uid, LocalDateTime.now())
        }

    override fun getStats(uid: String): LinkStats =
        findLink(uid).let { LinkStats(it.clickCount, it.lastClickedAt, it.botClickCount) }

    private fun findLink(uid: String): ShortLink = shortLinkStore.findById(uid)
        ?: throw NoSuchElementException("Short link '$uid' does not exist")

    companion object {
        private const val MAX_INSERT_ATTEMPTS = 3
    }
}
