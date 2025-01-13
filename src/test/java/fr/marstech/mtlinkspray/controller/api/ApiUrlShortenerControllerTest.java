package fr.marstech.mtlinkspray.controller.api;

import fr.marstech.mtlinkspray.service.ShortenerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiUrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortenerServiceImpl shortenerServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        when(shortenerServiceImpl.shorten(anyString())).thenReturn(new URL("https://short.url"));
    }

    @Test
    void getShort() throws Exception {
        mockMvc.perform(get("/api/url-shortener/shorten")
                        .param("url", "https://www.example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("https://short.url"));
    }
}