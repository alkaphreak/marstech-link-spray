package fr.marstech.mtlinkspray.domain.shortener
import fr.marstech.mtlinkspray.adapter.inbound.web.shortener.LinkStatsResponse
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shared.HistoryItem
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shortener.LinkItem
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shortener.LinkItemTarget
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shortener.LinkItemRepository
import fr.marstech.mtlinkspray.domain.shortener.ShortenerService.Companion.getShortenedLink
import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Map

@Service
class ShortenerServiceImpl(
    val linkItemRepository: LinkItemRepository,
    val randomIdGeneratorService: RandomIdGeneratorService
) : ShortenerService {

    override fun shorten(
        url: String,
        httpServletRequest: HttpServletRequest
    ): String? = getShortenedLink(
        uid = insertWithFreeId(url).id,
        httpServletRequest = httpServletRequest
    )

    // insert, not save: save() upserts, so an ID handed out twice would silently overwrite a link (MLS-246).
    private fun insertWithFreeId(url: String, attempt: Int = 1): LinkItem = try {
        linkItemRepository.insert(newLinkItem(randomIdGeneratorService.getGeneratedFreeId(), url))
    } catch (e: DuplicateKeyException) {
        if (attempt >= MAX_INSERT_ATTEMPTS) throw e
        insertWithFreeId(url, attempt + 1)
    }

    private fun newLinkItem(id: String, url: String) = LinkItem(
        id = id,
        creationDate = LocalDateTime.now(),
        expiresAt = null,
        isEnabled = true,
        description = null,
        metadata = Map.of<String, String>(),
        author = HistoryItem(),
        historyItems = mutableListOf(),
        target = LinkItemTarget(url)
    )

    override fun getTarget(
        uid: String,
        userAgent: String?,
    ): String = findLink(uid).target.targetUrl
        .also {
            if (BotUserAgents.isBot(userAgent)) linkItemRepository.findAndIncrementBotClickCountById(uid)
            else linkItemRepository.findAndIncrementClickCountById(uid, LocalDateTime.now())
        }

    override fun getStats(uid: String): LinkStatsResponse =
        findLink(uid).let { LinkStatsResponse(it.clickCount, it.lastClickedAt, it.botClickCount) }

    private fun findLink(uid: String): LinkItem = linkItemRepository.findById(uid)
        .orElseThrow { NoSuchElementException("Short link '$uid' does not exist") }

    companion object {
        private const val MAX_INSERT_ATTEMPTS = 3
    }
}
