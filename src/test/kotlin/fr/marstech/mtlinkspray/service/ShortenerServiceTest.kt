package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.config.TestConfig.MONGO_DB_DOCKER_IMAGE_NAME
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.LinkItem
import fr.marstech.mtlinkspray.entity.LinkItemTarget
import fr.marstech.mtlinkspray.repository.LinkItemRepository
import fr.marstech.mtlinkspray.utils.NetworkUtils.isValidUrl
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime
import java.util.*
import java.util.Map

@Testcontainers
@SpringBootTest
internal class ShortenerServiceTest(
    val linkItemRepository: LinkItemRepository,
    val shortenerService: ShortenerService,
    @MockitoBean var httpServletRequest: HttpServletRequest
) {

    @BeforeEach
    fun setUp() {
        Mockito.`when`(httpServletRequest.headerNames)
            .thenReturn(object : Enumeration<String?> {
                override fun hasMoreElements(): Boolean = false
                override fun nextElement(): String = ""
            })
        Mockito.`when`(httpServletRequest.serverName).thenReturn("localhost")
        Mockito.`when`(httpServletRequest.serverPort).thenReturn(8080)
        Mockito.`when`(httpServletRequest.scheme).thenReturn("http")
    }

    @Test
    fun linkItemTest() {
        val id = UUID.randomUUID().toString()
        LinkItem(
            id,
            LocalDateTime.now(),
            null,
            true,
            null,
            Map.of<String, String>(),
            HistoryItem(),
            mutableListOf<HistoryItem>(),
            LinkItemTarget("https://www.example.com")
        ).let { linkItemRepository.save(it) }
            .let { linkItemRepository.findById(it.id) }.let {
                Assertions.assertNotNull(it)
                Assertions.assertTrue(it.isPresent)
                Assertions.assertEquals(id, it.get().id)
            }
    }

    @Test
    fun shortenValidUrl() {
        shortenerService.shorten("https://www.example.com", httpServletRequest).let {
            Assertions.assertNotNull(it)
            Assertions.assertTrue(it.contains("http://localhost:8080/"))
            Assertions.assertTrue(isValidUrl(it))
        }
    }

    @Test
    fun shortenInvalidUrl() {
        val exception: Exception? = Assertions.assertThrows(
            IllegalArgumentException::class.java
        ) { shortenerService.shorten("invalid-url", httpServletRequest) }
        Assertions.assertInstanceOf(IllegalArgumentException::class.java, exception)
        Assertions.assertEquals("Invalid URL: invalid-url", exception!!.message)
    }

    @Test
    @Throws(Exception::class)
    fun getTargetValidUid() {
        val url = "https://www.example.com"
        val shortened = shortenerService.shorten(url, httpServletRequest)
        val uid = shortened.substring(shortened.lastIndexOf('/') + 1)
        val target = shortenerService.getTarget(uid, httpServletRequest)
        Assertions.assertEquals(url, target)
    }

    @Test
    fun getTargetInvalidUid_throwsException() {
        Assertions.assertThrows(
            ChangeSetPersister.NotFoundException::class.java
        ) { shortenerService.getTarget("nonexistent-uid", httpServletRequest) }
    }

    @Test
    @Throws(Exception::class)
    fun shortenAndRetrievePersistedLink() {
        val url = "https://persisted.com"
        val shortened = shortenerService.shorten(url, httpServletRequest)
        val uid = shortened.substring(shortened.lastIndexOf('/') + 1)
        val item = linkItemRepository.findById(uid)
        Assertions.assertTrue(item.isPresent)
        Assertions.assertEquals(url, item.get().target.targetUrl)
    }

    companion object {
        @Suppress("unused")
        @Container
        @ServiceConnection
        var mongoDBContainer: MongoDBContainer? =
            MongoDBContainer(DockerImageName.parse(MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true)
    }
}
