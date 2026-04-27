package fr.marstech.mtlinkspray.controller.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class RootApiControllerTest {

    private lateinit var mockMvc: MockMvc
    private val testVersion = "0.0.4-SNAPSHOT"

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(RootApiController(testVersion)).build()
    }

    @Test
    fun shouldReturnVersion() {
        mockMvc.perform(get("/api/version"))
            .andExpect(status().isOk)
            .andExpect(content().string(testVersion))
    }

    @Test
    fun shouldReturnVersionWithDifferentVersionString() {
        val customVersion = "1.0.0"
        val customMockMvc = MockMvcBuilders.standaloneSetup(RootApiController(customVersion)).build()
        
        customMockMvc.perform(get("/api/version"))
            .andExpect(status().isOk)
            .andExpect(content().string(customVersion))
    }
}