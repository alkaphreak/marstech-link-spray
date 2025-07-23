package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.controller.api.ApiRootController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ViewSprayController.class)
class ViewSprayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ApiRootController apiRootController;

    @Test
    void getHome() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/spray")).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getLink() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/spray").param("inputLinkList", "test")).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getSpray() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/spray/open").param("spray", "test")).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @TestConfiguration
    static class ApiRootControllerTestConfig {
        @Bean(name = "apiRootController")
        public ApiRootController apiRootController() {
            ApiRootController mock = org.mockito.Mockito.mock(ApiRootController.class);
            org.mockito.Mockito.when(mock.getVersion()).thenReturn("test-version");
            return mock;
        }
    }
}