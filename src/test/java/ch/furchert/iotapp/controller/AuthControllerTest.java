package ch.furchert.iotapp.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ch.furchert.iotapp.model.RefreshToken;
import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.repository.RoleRepository;
import ch.furchert.iotapp.repository.UserRepository;
import ch.furchert.iotapp.repository.UserStatusRepository;
import ch.furchert.iotapp.security.jwt.JwtUtils;
import ch.furchert.iotapp.service.EmailServiceImpl;
import ch.furchert.iotapp.service.EmailTokenService;
import ch.furchert.iotapp.service.RefreshTokenService;
import ch.furchert.iotapp.util.payload.request.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
//@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private UserStatusRepository userStatusRepository;

    @MockBean
    private PasswordEncoder encoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private EmailServiceImpl emailService;

    @MockBean
    private EmailTokenService emailTokenService;

    @Autowired
    private AuthController authController;


    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        // Initialize your mock setup here if needed
    }
    @Test
    public void testAuthenticateUser() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("username");
        loginRequest.setPassword("password");
        User user = new User();
        user.setUsername("username");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(jwtUtils.generateJwtToken(any())).thenReturn("some-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("some-jwt-token"));
    }
    @Test
    public void testAuthenticateUser2() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testUser");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(jwtUtils.generateJwtToken(any())).thenReturn("some-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUser\", \"password\":\"testPass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("some-jwt-token"));
    }

    @Test
    public void testRegisterUser() throws Exception {
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("newUser@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUser\", \"email\":\"newUser@example.com\", \"password\":\"newPass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    public void testVerifyEmail() throws Exception {
        when(emailTokenService.validateEmailToken("validToken")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/auth/verifyEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"validToken\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email successfully verified"));
    }

    @Test
    public void testLogoutUser() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer some-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You've been signed out!"));
    }

    @Test
    public void testRefreshToken() throws Exception {
        when(refreshTokenService.findByToken("validRefreshToken")).thenReturn(Optional.of(new RefreshToken()));

        mockMvc.perform(post("/api/auth/refreshtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"validRefreshToken\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    public void testAuthenticateUserUserNotFound() throws Exception {
        when(userRepository.findByUsername("invalidUser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"invalidUser\", \"password\":\"testPass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    public void testRegisterUserUsernameAlreadyExists() throws Exception {
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"existingUser\", \"email\":\"newUser@example.com\", \"password\":\"newPass\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    public void testVerifyEmailInvalidToken() throws Exception {
        when(emailTokenService.validateEmailToken("invalidToken")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/verifyEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"invalidToken\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    public void testRefreshTokenInvalidToken() throws Exception {
        when(refreshTokenService.findByToken("invalidRefreshToken")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refreshtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalidRefreshToken\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh Token is empty!"));
    }
}
