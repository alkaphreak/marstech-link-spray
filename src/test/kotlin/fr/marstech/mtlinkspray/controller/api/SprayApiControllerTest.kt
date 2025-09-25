package fr.marstech.mtlinkspray.controller.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
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

        val respectedResponse = "http://localhost/api/spray/open?spray=http://example.com&spray=http://example.org"

        get("/api/spray")
            .param("inputLinkList", "http://example.com", "http://example.org")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(
                content()
                    .string(respectedResponse)
            )
    }
}