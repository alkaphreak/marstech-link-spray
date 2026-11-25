package fr.marstech.mtlinkspray.adapter.outbound.mongo.shortener
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shared.HistoryItem
import fr.marstech.mtlinkspray.domain.shortener.ShortLink
import fr.marstech.mtlinkspray.domain.shortener.ShortLinkStore
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MongoShortLinkStore(private val repository: LinkItemRepository) : ShortLinkStore {

    // insert, not save: save() upserts, so an ID handed out twice would silently overwrite a link (MLS-246).
    override fun createWithFreeId(targetUrl: String, attempts: Int, newId: () -> String): ShortLink {
        var attempt = 1
        while (true) {
            try {
                return repository.insert(LinkItem.of(newId(), targetUrl)).toDomain()
            } catch (e: DuplicateKeyException) {
                if (attempt++ >= attempts) throw e
            }
        }
    }

    override fun findById(id: String): ShortLink? = repository.findById(id).map { it.toDomain() }.orElse(null)

    override fun existsById(id: String): Boolean = repository.existsById(id)

    override fun findAllIds(): Set<String> = repository.findAllIds().toSet()

    override fun countClick(id: String, clickedAt: LocalDateTime) {
        repository.findAndIncrementClickCountById(id, clickedAt)
    }

    override fun countBotClick(id: String) {
        repository.findAndIncrementBotClickCountById(id)
    }
}

fun LinkItem.toDomain() = ShortLink(
    id = id,
    targetUrl = target.targetUrl,
    creationDate = creationDate,
    clickCount = clickCount,
    lastClickedAt = lastClickedAt,
    botClickCount = botClickCount,
)

/** A new enabled link with no author, history or metadata, as the shortener has always stored them. */
fun LinkItem.Companion.of(id: String, targetUrl: String) = LinkItem(
    id = id,
    creationDate = LocalDateTime.now(),
    expiresAt = null,
    isEnabled = true,
    description = null,
    metadata = java.util.Map.of<String, String>(),
    author = HistoryItem(),
    historyItems = mutableListOf(),
    target = LinkItemTarget(targetUrl),
)
