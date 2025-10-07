package fr.marstech.mtlinkspray

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class MtLinkSprayApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(MtLinkSprayApplication::class.java, *args)
        }
    }
}
