package fr.marstech.mtlinkspray


import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class MtLinkSprayApplicationTests {
    @Test
    fun contextLoads() {
        assert(true)
    }
}
