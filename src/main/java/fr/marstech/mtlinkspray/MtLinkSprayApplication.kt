package fr.marstech.mtlinkspray

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
object MtLinkSprayApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(MtLinkSprayApplication::class.java, *args)
    }
}
