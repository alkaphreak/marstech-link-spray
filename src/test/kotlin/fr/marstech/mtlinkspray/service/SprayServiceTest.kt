package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.service.SprayService.Companion.getLinkList
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*

@SpringBootTest
internal class SprayServiceTest {

    @MockitoBean
    lateinit var httpServletRequest: HttpServletRequest

    @BeforeEach
    fun setUp() {
        `when`(httpServletRequest.headerNames).thenReturn(object : Enumeration<String?> {
            override fun hasMoreElements(): Boolean = false
            override fun nextElement(): String = ""
        })
        `when`(httpServletRequest.serverName).thenReturn("localhost")
        `when`(httpServletRequest.serverPort).thenReturn(8080)
        `when`(httpServletRequest.scheme).thenReturn("http")
        `when`(httpServletRequest.requestURI).thenReturn("spray")
    }

    @Test
    fun testGetLinkList() {
        val inputLinkList = """
                https://www.google.com
                https://www.facebook.com
                https://www.twitter.com
                """.trimIndent()
        val linkList: List<String> = getLinkList(inputLinkList)
        Assertions.assertEquals(3, linkList.size)
        Assertions.assertEquals("https://www.google.com", linkList[0])
        Assertions.assertEquals("https://www.facebook.com", linkList[1])
        Assertions.assertEquals("https://www.twitter.com", linkList[2])
    }

    @Test
    fun testGetLinkListText() {
        val inputLinkList = """
                https://www.google.com
                https://www.facebook.com
                https://www.twitter.com
                """.trimIndent()
        val linkList: List<String> = getLinkList(inputLinkList)
        val linkListText = SprayService.getLinkListText(linkList)
        Assertions.assertEquals(inputLinkList, linkListText)
    }

    @Test
    fun testGetLinkSpray() {
        val inputLinkList = """
                https://www.google.com
                https://www.facebook.com
                https://www.twitter.com
                """.trimIndent()
        val linkList: List<String> = getLinkList(inputLinkList)
        val linkSpray = SprayService.getLinkSpray(httpServletRequest, linkList)
        Assertions.assertEquals(
            "http://localhost:8080/spray/open?spray=https://www.google.com&spray=https://www.facebook.com&spray=https://www.twitter.com",
            linkSpray
        )
    }
}
