package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.LinkItem
import fr.marstech.mtlinkspray.entity.LinkItemTarget
import fr.marstech.mtlinkspray.repository.LinkItemRepository
import fr.marstech.mtlinkspray.service.ShortenerService.Companion.getShortenedLink
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
    ): String =
        linkItemRepository.findById(uid)
            .orElseThrow { NoSuchElementException("Short link '$uid' does not exist") }
            .target
            .targetUrl

    companion object {
        private const val MAX_INSERT_ATTEMPTS = 3
    }
}
