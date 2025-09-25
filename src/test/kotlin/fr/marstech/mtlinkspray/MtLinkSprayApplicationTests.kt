package fr.marstech.mtlinkspray

import fr.marstech.mtlinkspray.MtLinkSprayApplication.Companion.main
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class MtLinkSprayApplicationTests {
    @Test
    fun contextLoads() {
        assert(true)
    }

    @Test
    fun testMain() {
        main(arrayOf("args"))
    }
}
