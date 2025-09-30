package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.api.RootApiController
import fr.marstech.mtlinkspray.controller.view.ViewRootController
import fr.marstech.mtlinkspray.enums.ViewNameEnum.HOME
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@Disabled
@WebMvcTest(controllers = [ViewRootController::class])
class ViewRootControllerTest(@Autowired val mockMvc: MockMvc) {

    @TestConfiguration
    open class MockApiRootControllerConfig {
        @Bean(name = ["rootApiController"])
        open fun rootApiController(): RootApiController {
            val mock = Mockito.mock(RootApiController::class.java)
            Mockito.`when`(mock.version).thenReturn("1.0.0")
            return mock
        }
    }

    @Test
    fun `GET home returns expected view`() {
        mockMvc.get("/").andExpect {
            status { isOk() }
            view { name(HOME.viewName) }
        }
    }

    @Test
    fun `GET index returns expected view`() {
        mockMvc.get("/index").andExpect {
            status { isOk() }
            view { name(HOME.viewName) }
        }
    }
}