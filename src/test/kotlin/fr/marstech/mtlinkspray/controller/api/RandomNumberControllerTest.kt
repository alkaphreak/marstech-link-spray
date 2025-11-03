package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class RandomNumberControllerTest {

    private val controller = RandomNumberApiController()
    private val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(GlobalRestExceptionHandler())
        .build()

    @Test
    fun shouldGenerateRandomNumberBetweenMinAndMax() {
        mockMvc.perform(get("/api/random?min=10&max=20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.min").value(10))
            .andExpect(jsonPath("$.max").value(20))
            .andExpect(jsonPath("$.value").isNumber)
    }

    @Test
    fun shouldDefaultMinToZeroWhenNotProvided() {
        mockMvc.perform(get("/api/random?max=50"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.min").value(0))
            .andExpect(jsonPath("$.max").value(50))
    }

    @Test
    fun shouldReturnBadRequestWhenMaxIsMissing() {
        mockMvc.perform(get("/api/random"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnBadRequestWhenMinGreaterThanMax() {
        mockMvc.perform(get("/api/random?min=100&max=10"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnBadRequestWhenMaxIsNotANumber() {
        // non-numeric value should fail type conversion and result in 400
        mockMvc.perform(get("/api/random?max=notanumber"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnBadRequestWhenMaxIsTooLarge() {
        // value larger than Integer.MAX_VALUE should fail conversion
        mockMvc.perform(get("/api/random?max=1000000000000"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnBadRequestWhenMaxExceedsConfiguredLimit() {
        // controller enforces max value limit via manual validation (string length and numeric value), not via @Max annotation
        mockMvc.perform(get("/api/random?max=1000000001"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnBadRequestWhenMinBelowConfiguredLimit() {
        // controller enforces min parameter via manual length validation (up to 9 characters), not via @Min annotation
        mockMvc.perform(get("/api/random?min=-1000000001&max=10"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldAcceptBoundaryMinMaxValues() {
        // boundary values defined by annotations should be accepted
        mockMvc.perform(get("/api/random?min=-10000000&max=100000000"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.min").value(-10000000))
            .andExpect(jsonPath("$.max").value(100000000))
    }
}
