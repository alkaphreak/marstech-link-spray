package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [SleepApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class SleepApiControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun shouldReturnBedtimesForWakeAt() {
        // Given / When / Then
        mockMvc.perform(get("/api/sleep").param("wakeAt", "07:00"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mode").value("WAKE_AT"))
            .andExpect(jsonPath("$.options[0].cycles").value(6))
    }

    @Test
    fun shouldReturnWakeTimesForSleepAt() {
        // Given / When / Then
        mockMvc.perform(get("/api/sleep").param("sleepAt", "23:00"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mode").value("SLEEP_AT"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "wakeAt=07:00&sleepAt=23:00", "wakeAt=7h", "sleepAt=25:00"])
    fun shouldRejectMissingBothOrMalformedTimes(query: String) {
        // Given / When / Then
        mockMvc.perform(get("/api/sleep?$query"))
            .andExpect(status().isBadRequest)
    }
}
