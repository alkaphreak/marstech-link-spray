package fr.marstech.mtlinkspray.domain.shortener
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ShortenerService(
    private val shortLinkStore: ShortLinkStore,
    private val randomIdGeneratorService: RandomIdGeneratorService,
    private val botUserAgents: BotUserAgents,
) {

    /**
     * Get the shortened URL of the target URL. Never overwrites: an ID handed out twice draws a new
     * one instead (MLS-246).
     *
     * @param url   the target URL
     * @param links builds the short URL from the new link's id
     * @return the shortened URL
     */
    fun shorten(url: String, links: LinkUrls): String? = links.shortLink(
        shortLinkStore.createWithFreeId(url, MAX_INSERT_ATTEMPTS, randomIdGeneratorService::getGeneratedFreeId).id
    )

    /**
     * Get the target URL from the shortened UID and count the click, as a robot's when the User-Agent says so
     *
     * @param uid       the shortened UID
     * @param userAgent the request's User-Agent, read in memory and never stored (MLS-273)
     * @return the target URL
     */
    fun getTarget(
        uid: String,
        userAgent: String?,
    ): String = findLink(uid).targetUrl
        .also {
            if (botUserAgents.isBot(userAgent)) shortLinkStore.countBotClick(uid)
            else shortLinkStore.countClick(uid, LocalDateTime.now())
        }

    /** Click counts (people, robots) and last click date of a short link (MLS-261, MLS-273). */
    fun getStats(uid: String): LinkStats =
        findLink(uid).let { LinkStats(it.clickCount, it.lastClickedAt, it.botClickCount) }

    private fun findLink(uid: String): ShortLink = shortLinkStore.findById(uid)
        ?: throw NoSuchElementException("Short link '$uid' does not exist")

    companion object {
        private const val MAX_INSERT_ATTEMPTS = 3
    }
}
