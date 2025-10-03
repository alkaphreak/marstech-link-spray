package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.LinkItem
import fr.marstech.mtlinkspray.entity.LinkItemTarget
import fr.marstech.mtlinkspray.repository.LinkItemRepository
import fr.marstech.mtlinkspray.utils.NetworkUtils.isValidUrl
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.crossstore.ChangeSetPersister
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class ShortenerServiceTest {
    private val linkItemRepository = mock<LinkItemRepository>()
    private val httpServletRequest = mock<HttpServletRequest>()
    private val randomIdGeneratorService = mock<RandomIdGeneratorService>()
    private val shortenerService = ShortenerServiceImpl(linkItemRepository, randomIdGeneratorService)

    @BeforeEach
    fun setUp() {
        whenever(httpServletRequest.headerNames)
            .thenReturn(object : Enumeration<String?> {
                override fun hasMoreElements(): Boolean = false
                override fun nextElement(): String = ""
            })
        whenever(httpServletRequest.serverName).thenReturn("localhost")
        whenever(httpServletRequest.serverPort).thenReturn(8080)
        whenever(httpServletRequest.scheme).thenReturn("http")
        whenever(randomIdGeneratorService.getGeneratedFreeId()).thenReturn("test-id")
    }

    @Test
    fun linkItemTest() {
        val id = UUID.randomUUID().toString()
        val linkItem = LinkItem(
            id,
            LocalDateTime.now(),
            null,
            true,
            null,
            mutableMapOf(),
            HistoryItem(),
            mutableListOf(),
            LinkItemTarget("https://www.example.com")
        )
        whenever(linkItemRepository.save(any())).thenReturn(linkItem)
        whenever(linkItemRepository.findById(id)).thenReturn(Optional.of(linkItem))
        linkItemRepository.save(linkItem)
        val found = linkItemRepository.findById(id)
        Assertions.assertNotNull(found)
        Assertions.assertTrue(found.isPresent)
        Assertions.assertEquals(id, found.get().id)
    }

    @Test
    fun shortenValidUrl() {
        val id = "uid"
        val linkItem = LinkItem(
            id,
            LocalDateTime.now(),
            null,
            true,
            null,
            mutableMapOf(),
            HistoryItem(),
            mutableListOf(),
            LinkItemTarget("https://www.example.com")
        )
        whenever(linkItemRepository.save(any())).thenReturn(linkItem)
        whenever(linkItemRepository.findById(id)).thenReturn(Optional.of(linkItem))
        whenever(randomIdGeneratorService.getGeneratedFreeId()).thenReturn(id)
        shortenerService.shorten("https://www.example.com", httpServletRequest).let {
            Assertions.assertNotNull(it)
            Assertions.assertTrue(it.contains("http://localhost:8080/"))
            Assertions.assertTrue(isValidUrl(it))
        }
    }

    @Test
    fun shortenInvalidUrl() {
        whenever(randomIdGeneratorService.getGeneratedFreeId()).thenReturn("invalid-id")
        whenever(linkItemRepository.save(any())).thenReturn(null)
        val exception: Exception? = Assertions.assertThrows(
            NullPointerException::class.java
        ) { shortenerService.shorten("invalid-url", httpServletRequest) }
        Assertions.assertInstanceOf(NullPointerException::class.java, exception)
    }

    @Test
    fun getTargetValidUid() {
        val id = "uid"
        val url = "https://www.example.com"
        val linkItem = LinkItem(
            id,
            LocalDateTime.now(),
            null,
            true,
            null,
            mutableMapOf(),
            HistoryItem(),
            mutableListOf(),
            LinkItemTarget(url)
        )
        whenever(linkItemRepository.save(any())).thenReturn(linkItem)
        whenever(linkItemRepository.findById(id)).thenReturn(Optional.of(linkItem))
        whenever(randomIdGeneratorService.getGeneratedFreeId()).thenReturn(id)
        val shortened = shortenerService.shorten(url, httpServletRequest)
        val uid = shortened.substring(shortened.lastIndexOf('/') + 1)
        val target = shortenerService.getTarget(uid)
        Assertions.assertEquals(url, target)
    }

    @Test
    fun getTargetInvalidUid_throwsException() {
        whenever(linkItemRepository.findById(any())).thenReturn(Optional.empty())
        Assertions.assertThrows(
            ChangeSetPersister.NotFoundException::class.java
        ) { shortenerService.getTarget("nonexistent-uid") }
    }

    @Test
    fun shortenAndRetrievePersistedLink() {
        val id = "uid"
        val url = "https://persisted.com"
        val linkItem = LinkItem(
            id,
            LocalDateTime.now(),
            null,
            true,
            null,
            mutableMapOf(),
            HistoryItem(),
            mutableListOf(),
            LinkItemTarget(url)
        )
        whenever(linkItemRepository.save(any())).thenReturn(linkItem)
        whenever(linkItemRepository.findById(id)).thenReturn(Optional.of(linkItem))
        whenever(randomIdGeneratorService.getGeneratedFreeId()).thenReturn(id)
        val shortened = shortenerService.shorten(url, httpServletRequest)
        val uid = shortened.substring(shortened.lastIndexOf('/') + 1)
        val item = linkItemRepository.findById(uid)
        Assertions.assertTrue(item.isPresent)
        Assertions.assertEquals(url, item.get().target.targetUrl)
    }
}
