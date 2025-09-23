package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.controller.api.RootApiController;
import fr.marstech.mtlinkspray.controller.view.ViewSprayController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ViewSprayController.class)
class ViewSprayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSprayPage() throws Exception {
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
        public RootApiController apiRootController() {
            RootApiController mock = mock(RootApiController.class);
            when(mock.getVersion()).thenReturn("test-version");
            return mock;
        }
    }
}