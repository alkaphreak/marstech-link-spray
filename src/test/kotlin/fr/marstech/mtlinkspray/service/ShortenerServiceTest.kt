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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DuplicateKeyException
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
        whenever(linkItemRepository.insert(any<LinkItem>())).thenReturn(linkItem)
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
        whenever(linkItemRepository.insert(any<LinkItem>())).thenReturn(linkItem)
        whenever(linkItemRepository.findById(id)).thenReturn(Optional.of(linkItem))
        whenever(randomIdGeneratorService.getGeneratedFreeId()).thenReturn(id)
        shortenerService.shorten("https://www.example.com", httpServletRequest).let {
            Assertions.assertNotNull(it)
            val shortenedUrl = it!!
            Assertions.assertTrue(shortenedUrl.contains("http://localhost:8080/"))
            Assertions.assertTrue(isValidUrl(shortenedUrl))
        }
    }

    @Test
    fun shortenInvalidUrl() {
        whenever(randomIdGeneratorService.getGeneratedFreeId()).thenReturn("invalid-id")
        whenever(linkItemRepository.insert(any<LinkItem>())).thenReturn(null)
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
        whenever(linkItemRepository.insert(any<LinkItem>())).thenReturn(linkItem)
        whenever(linkItemRepository.findById(id)).thenReturn(Optional.of(linkItem))
        whenever(randomIdGeneratorService.getGeneratedFreeId()).thenReturn(id)
        val shortened = shortenerService.shorten(url, httpServletRequest)
        val uid = shortened!!.substring(shortened.lastIndexOf('/') + 1)
        val target = shortenerService.getTarget(uid)
        Assertions.assertEquals(url, target)
    }

    @Test
    fun getTargetInvalidUid_throwsException() {
        whenever(linkItemRepository.findById(any())).thenReturn(Optional.empty())
        val exception = Assertions.assertThrows(
            NoSuchElementException::class.java
        ) { shortenerService.getTarget("nonexistent-uid") }
        Assertions.assertEquals("Short link 'nonexistent-uid' does not exist", exception.message)
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
        whenever(linkItemRepository.insert(any<LinkItem>())).thenReturn(linkItem)
        whenever(linkItemRepository.findById(id)).thenReturn(Optional.of(linkItem))
        whenever(randomIdGeneratorService.getGeneratedFreeId()).thenReturn(id)
        val shortened = shortenerService.shorten(url, httpServletRequest)
        val uid = shortened!!.substring(shortened.lastIndexOf('/') + 1)
        val item = linkItemRepository.findById(uid)
        Assertions.assertTrue(item.isPresent)
        Assertions.assertEquals(url, item.get().target.targetUrl)
    }

    @Test
    fun shortenShouldRetryWithNewIdWhenIdIsAlreadyTaken() {
        whenever(randomIdGeneratorService.getGeneratedFreeId()).thenReturn("taken", "free")
        whenever(linkItemRepository.insert(any<LinkItem>())).thenAnswer {
            val item = it.getArgument<LinkItem>(0)
            if (item.id == "taken") throw DuplicateKeyException("E11000 duplicate key") else item
        }

        val shortened = shortenerService.shorten("https://www.example.com", httpServletRequest)

        Assertions.assertTrue(shortened!!.endsWith("/free"))
        verify(linkItemRepository, never()).save(any<LinkItem>())
    }

    @Test
    fun shortenShouldGiveUpAfterMaxDuplicateIds() {
        whenever(linkItemRepository.insert(any<LinkItem>())).thenThrow(DuplicateKeyException("E11000 duplicate key"))

        Assertions.assertThrows(DuplicateKeyException::class.java) {
            shortenerService.shorten("https://www.example.com", httpServletRequest)
        }
        verify(linkItemRepository, times(3)).insert(argThat<LinkItem> { id == "test-id" })
    }
}
