package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.controller.api.RandomNumberApiController
import fr.marstech.mtlinkspray.controller.api.RootApiController
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.controller.view.ViewQrController
import fr.marstech.mtlinkspray.dto.RandomNumberResponse
import fr.marstech.mtlinkspray.service.RandomNumberService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

// API versioning by content negotiation (MLS-137, docs/adr/0001): the application.properties
// setup, checked on one JSON endpoint and one Thymeleaf page
@WebMvcTest(controllers = [RandomNumberApiController::class, ViewQrController::class, RootApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class ApiVersioningTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var randomNumberService: RandomNumberService

    @BeforeEach
    fun setup() {
        whenever(randomNumberService.generate(anyOrNull(), anyOrNull()))
            .thenReturn(RandomNumberResponse(value = 4, min = 1, max = 6, timestamp = Instant.EPOCH))
    }

    @Test
    fun requestWithoutVersionIsServedAsV1() {
        mockMvc.perform(get("/api/random"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.value").value(4))
    }

    @Test
    fun requestWithVersion1MediaTypeParameterIsServed() {
        mockMvc.perform(get("/api/random").accept("application/json;version=1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.value").value(4))
    }

    @Test
    fun unsupportedVersionReturns400() {
        mockMvc.perform(get("/api/random").accept("application/json;version=9"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun malformedVersionReturns400() {
        mockMvc.perform(get("/api/random").accept("application/json;version=abc"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun htmlPageIsUnaffected() {
        mockMvc.perform(get("/qr").accept(TEXT_HTML))
            .andExpect(status().isOk)
    }
}
