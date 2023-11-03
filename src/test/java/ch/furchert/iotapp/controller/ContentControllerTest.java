package ch.furchert.iotapp.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAllAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/get/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("Public Content."));
    }
}
