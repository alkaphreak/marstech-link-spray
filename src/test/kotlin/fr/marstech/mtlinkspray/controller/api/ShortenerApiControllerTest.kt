package fr.marstech.mtlinkspray.controller.api;

import fr.marstech.mtlinkspray.service.ShortenerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShortenerApiControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ShortenerServiceImpl shortenerServiceImpl;

  @BeforeEach
  void setUp() throws Exception {
    when(shortenerServiceImpl.shorten(anyString(), any()))
        .thenReturn(URI.create("https://short.url").toURL().toString());
  }

  @Test
  void getShort() throws Exception {
    mockMvc
        .perform(get("/api/url-shortener/shorten").param("url", "https://www.example.com"))
        .andExpect(status().isOk())
        .andExpect(content().string("https://short.url"));
  }
}
