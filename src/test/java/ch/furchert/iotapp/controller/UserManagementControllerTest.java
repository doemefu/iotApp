package ch.furchert.iotapp.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testGetUserById() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user-management/showUser/1"))
                .andExpect(status().isOk());
    }



}
