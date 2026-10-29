package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.dto.IpInfoResponse
import fr.marstech.mtlinkspray.service.IpService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [IpApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class IpApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var ipService: IpService

    @Test
    fun shouldLookUpCallerFromCloudflareHeader() {
        // Given
        `when`(ipService.lookup(CLIENT_IP)).thenReturn(info(CLIENT_IP))

        // When / Then
        mockMvc.perform(get(IP_PATH).header("CF-Connecting-IP", CLIENT_IP))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ip").value(CLIENT_IP))
            .andExpect(jsonPath("$.countryCode").value("FR"))
    }

    @Test
    fun shouldLookUpGivenIp() {
        // Given
        `when`(ipService.lookup("1.1.1.1")).thenReturn(info("1.1.1.1"))

        // When / Then
        mockMvc.perform(get(IP_PATH).param("ip", "1.1.1.1").header("CF-Connecting-IP", CLIENT_IP))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ip").value("1.1.1.1"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["example.com", "999.1.1.1", "ldap://x"])
    fun shouldRejectNonIp(ip: String) {
        // Given / When / Then
        mockMvc.perform(get(IP_PATH).param("ip", ip)).andExpect(status().isBadRequest)
    }

    private fun info(ip: String) = IpInfoResponse(ip, 4, "FR", "France", 3215L, "Orange", datacenter = false)

    companion object {
        const val IP_PATH = "/api/ip"
        const val CLIENT_IP = "203.0.113.9"
    }
}
