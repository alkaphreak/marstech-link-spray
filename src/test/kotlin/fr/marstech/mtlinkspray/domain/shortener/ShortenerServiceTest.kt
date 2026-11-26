package fr.marstech.mtlinkspray.domain.shortener
import fr.marstech.mtlinkspray.adapter.inbound.web.shortener.RequestLinkUrls
import fr.marstech.mtlinkspray.domain.shared.ForbiddenException
import fr.marstech.mtlinkspray.adapter.outbound.botagents.ClasspathBotUserAgentSource
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shortener.MongoShortLinkStore
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shared.HistoryItem
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shortener.LinkItem
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shortener.LinkItemTarget
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shortener.LinkItemRepository
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils.isValidUrl
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DuplicateKeyException
import java.time.LocalDateTime
import java.util.*

private const val BROWSER_UA =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

@ExtendWith(MockitoExtension::class)
internal class ShortenerServiceTest {
    private val linkItemRepository = mock<LinkItemRepository>()
    private val httpServletRequest = mock<HttpServletRequest>()
    private val randomIdGeneratorService = mock<RandomIdGeneratorService>()
    private val urlReputation = mock<UrlReputationPort>()
    private val shortenerService = ShortenerService(
        MongoShortLinkStore(linkItemRepository), randomIdGeneratorService, BotUserAgents(ClasspathBotUserAgentSource()),
        urlReputation,
    )

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
        shortenerService.shorten("https://www.example.com", RequestLinkUrls(httpServletRequest)).let {
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
        ) { shortenerService.shorten("invalid-url", RequestLinkUrls(httpServletRequest)) }
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
        val shortened = shortenerService.shorten(url, RequestLinkUrls(httpServletRequest))
        val uid = shortened!!.substring(shortened.lastIndexOf('/') + 1)
        val target = shortenerService.getTarget(uid, BROWSER_UA)
        Assertions.assertEquals(url, target)
    }

    @Test
    fun getTargetInvalidUid_throwsException() {
        whenever(linkItemRepository.findById(any())).thenReturn(Optional.empty())
        val exception = Assertions.assertThrows(
            NoSuchElementException::class.java
        ) { shortenerService.getTarget("nonexistent-uid", BROWSER_UA) }
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
        val shortened = shortenerService.shorten(url, RequestLinkUrls(httpServletRequest))
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

        val shortened = shortenerService.shorten("https://www.example.com", RequestLinkUrls(httpServletRequest))

        Assertions.assertTrue(shortened!!.endsWith("/free"))
        verify(linkItemRepository, never()).save(any<LinkItem>())
    }

    @Test
    fun shortenShouldGiveUpAfterMaxDuplicateIds() {
        whenever(linkItemRepository.insert(any<LinkItem>())).thenThrow(DuplicateKeyException("E11000 duplicate key"))

        Assertions.assertThrows(DuplicateKeyException::class.java) {
            shortenerService.shorten("https://www.example.com", RequestLinkUrls(httpServletRequest))
        }
        verify(linkItemRepository, times(3)).insert(argThat<LinkItem> { id == "test-id" })
    }
    private fun storedLink(id: String, clicks: Long = 0, lastClick: LocalDateTime? = null, botClicks: Long = 0) = LinkItem(
        id = id, author = HistoryItem(), target = LinkItemTarget("https://www.example.com"),
        clickCount = clicks, lastClickedAt = lastClick, botClickCount = botClicks,
    )

    @Test
    fun getTargetShouldCountTheClick() {
        whenever(linkItemRepository.findById("abc")).thenReturn(Optional.of(storedLink("abc")))

        Assertions.assertEquals("https://www.example.com", shortenerService.getTarget("abc", BROWSER_UA))
        verify(linkItemRepository).findAndIncrementClickCountById(eq("abc"), any())
    }

    @Test
    fun getTargetFromRobotShouldCountABotClickOnly() {
        whenever(linkItemRepository.findById("abc")).thenReturn(Optional.of(storedLink("abc")))

        Assertions.assertEquals("https://www.example.com", shortenerService.getTarget("abc", "Slackbot-LinkExpanding 1.0"))
        verify(linkItemRepository).findAndIncrementBotClickCountById("abc")
        verify(linkItemRepository, never()).findAndIncrementClickCountById(any(), any())
    }

    @Test
    fun getTargetFromBrowserShouldNotCountABotClick() {
        whenever(linkItemRepository.findById("abc")).thenReturn(Optional.of(storedLink("abc")))

        shortenerService.getTarget("abc", BROWSER_UA)
        verify(linkItemRepository, never()).findAndIncrementBotClickCountById(any())
    }

    @Test
    fun getTargetOfUnknownLinkShouldThrowWithoutCounting() {
        whenever(linkItemRepository.findById("nope")).thenReturn(Optional.empty())

        Assertions.assertThrows(NoSuchElementException::class.java) { shortenerService.getTarget("nope", BROWSER_UA) }
        verify(linkItemRepository, never()).findAndIncrementClickCountById(any(), any())
        verify(linkItemRepository, never()).findAndIncrementBotClickCountById(any())
    }

    @Test
    fun getStatsShouldReturnTheStoredCounter() {
        val lastClick = LocalDateTime.of(2026, 11, 9, 12, 0)
        whenever(linkItemRepository.findById("abc")).thenReturn(Optional.of(storedLink("abc", 42, lastClick, 7)))

        Assertions.assertEquals(LinkStats(42, lastClick, 7), shortenerService.getStats("abc"))
    }

    @Test
    fun shortenShouldRefuseAMaliciousUrlWithoutStoringIt() {
        whenever(urlReputation.anyMalicious(listOf("https://evil.example"))).thenReturn(true)

        val e = Assertions.assertThrows(ForbiddenException::class.java) {
            shortenerService.shorten("https://evil.example", RequestLinkUrls(httpServletRequest))
        }
        Assertions.assertEquals(ShortenerService.MALICIOUS_URL, e.message)
        verify(linkItemRepository, never()).insert(any<LinkItem>())
    }
}
