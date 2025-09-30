package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.LinkItem
import fr.marstech.mtlinkspray.entity.LinkItemTarget
import fr.marstech.mtlinkspray.repository.LinkItemRepository
import fr.marstech.mtlinkspray.service.ShortenerService.Companion.getShortenedLink
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Map
import java.util.function.Supplier

@Service
class ShortenerServiceImpl(
    val linkItemRepository: LinkItemRepository,
    val randomIdGeneratorService: RandomIdGeneratorService
) : ShortenerService {

    override fun shorten(
        url: String,
        httpServletRequest: HttpServletRequest
    ): String = LinkItem(
        id = randomIdGeneratorService.getGeneratedFreeId(),
        creationDate = LocalDateTime.now(),
        expiresAt = null,
        isEnabled = true,
        description = null,
        metadata = Map.of<String, String>(),
        author = HistoryItem(),
        historyItems = mutableListOf<HistoryItem>(),
        target = LinkItemTarget(url)
    ).let { linkItemRepository.save(it) }
        .let { getShortenedLink(httpServletRequest, it.id) }

    override fun getTarget(
        uid: String,
    ): String =
        linkItemRepository.findById(uid)
            .orElseThrow<ChangeSetPersister.NotFoundException?>(
                Supplier { ChangeSetPersister.NotFoundException() })
            .target
            .targetUrl
}
