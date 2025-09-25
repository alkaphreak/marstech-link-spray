package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.api.RootApiController
import fr.marstech.mtlinkspray.controller.view.ViewSprayController
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(ViewSprayController::class)
class ViewSprayControllerTest(@Autowired val mockMvc: MockMvc) {

    @TestConfiguration
    open class ApiRootControllerTestConfig {
        @Bean(name = ["rootApiController"])
        open fun rootApiController(): RootApiController {
            val mock = Mockito.mock(RootApiController::class.java)
            Mockito.`when`(mock.version).thenReturn("test-version")
            return mock
        }
    }

    @Test
    fun `GET spray page returns OK`() {
        mockMvc.get("/spray").andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `POST spray returns OK`() {
        mockMvc.post("/spray") {
            param("inputLinkList", "test")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `GET spray open returns OK`() {
        mockMvc.get("/spray/open") {
            param("spray", "test")
        }.andExpect {
            status { isOk() }
        }
    }
}