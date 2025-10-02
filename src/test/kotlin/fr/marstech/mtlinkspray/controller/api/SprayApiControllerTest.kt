package fr.marstech.mtlinkspray.controller.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SprayApiController::class)
class SprayApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun getSprayReturnsExpectedResult() {

        val respectedResponse = "http://localhost/api/spray/open?spray=https://example.com&spray=https://example.org"

        get("/api/spray")
            .param("inputLinkList", "https://example.com", "https://example.org")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(
                content()
                    .string(respectedResponse)
            )
    }
}