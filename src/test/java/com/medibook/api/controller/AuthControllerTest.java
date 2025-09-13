package com.medibook.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.api.dto.RegisterRequestDTO;
import com.medibook.api.dto.RegisterResponseDTO;
import com.medibook.api.dto.SignInRequestDTO;
import com.medibook.api.dto.SignInResponseDTO;
import com.medibook.api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(AuthControllerTest.MockConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService; 

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        public AuthService authService() {
            return org.mockito.Mockito.mock(AuthService.class);
        }
    }

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.reset(authService);
    }

    @Test
    @WithMockUser
    void whenRegisterPatient_thenSuccess() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "patient@example.com",
            "password123",
            "John",
            "Doe",
            "+1234567890",
            LocalDate.of(1990, 1, 1),
            "MALE",
            null,
            null,
            null
        );

        RegisterResponseDTO response = new RegisterResponseDTO(
            UUID.randomUUID(),
            request.email(),
            request.name(),
            request.surname(),
            "PATIENT"
        );

        when(authService.registerPatient(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register/patient")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value(request.email()))
            .andExpect(jsonPath("$.role").value("PATIENT"));
    }

    @Test
    @WithMockUser
    void whenRegisterDoctor_thenSuccess() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "doctor@example.com",
            "password123",
            "John",
            "Doe",
            "+1234567890",
            LocalDate.of(1990, 1, 1),
            "MALE",
            "ML123",
            "Cardiology",
            30
        );

        RegisterResponseDTO response = new RegisterResponseDTO(
            UUID.randomUUID(),
            request.email(),
            request.name(),
            request.surname(),
            "DOCTOR"
        );

        when(authService.registerDoctor(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register/doctor")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value(request.email()))
            .andExpect(jsonPath("$.role").value("DOCTOR"));
    }

    @Test
    @WithMockUser
    void whenRegisterWithExistingEmail_thenBadRequest() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "existing@example.com",
            "password123",
            "John",
            "Doe",
            null,
            null,
            null,
            null,
            null,
            null
        );

        when(authService.registerPatient(any(RegisterRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Email already registered"));

        mockMvc.perform(post("/api/auth/register/patient")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Email already registered"));
    }

    @Test
    @WithMockUser
    void whenRegisterDoctorWithoutLicense_thenBadRequest() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "doctor@example.com",
            "password123",
            "John",
            "Doe",
            null,
            null,
            null,
            null,
            null,
            null
        );

        when(authService.registerDoctor(any(RegisterRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Medical license and specialty are required for doctors"));

        mockMvc.perform(post("/api/auth/register/doctor")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Medical license and specialty are required for doctors"));
    }

    @Test
    @WithMockUser
    void whenInvalidRequest_thenBadRequest() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "invalid-email",
            "short",
            "",
            "",
            null,
            null,
            null,
            null,
            null,
            null
        );

        mockMvc.perform(post("/api/auth/register/patient")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void whenSignIn_thenSuccess() throws Exception {
        SignInRequestDTO request = new SignInRequestDTO(
            "user@example.com",
            "password123"
        );

        SignInResponseDTO response = new SignInResponseDTO(
            UUID.randomUUID(),
            request.email(),
            "John",
            "Doe",
            "PATIENT",
            "access-token",
            "refresh-token"
        );

        when(authService.signIn(any(SignInRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/signin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value(request.email()))
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @WithMockUser
    void whenSignInWithInvalidCredentials_thenBadRequest() throws Exception {
        SignInRequestDTO request = new SignInRequestDTO(
            "user@example.com",
            "wrongpassword"
        );

        when(authService.signIn(any(SignInRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/signin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid email or password"));
    }

    @Test
    @WithMockUser
    void whenSignOut_thenSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/signout")
                .with(csrf())
                .header("Refresh-Token", "refresh-token-123"))
            .andExpect(status().isOk());

        verify(authService).signOut("refresh-token-123");
    }

    @Test
    @WithMockUser
    void whenRefreshToken_thenSuccess() throws Exception {
        SignInResponseDTO response = new SignInResponseDTO(
            UUID.randomUUID(),
            "user@example.com",
            "John",
            "Doe",
            "PATIENT",
            "new-access-token",
            "new-refresh-token"
        );

        when(authService.refreshToken("refresh-token-123")).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh-token")
                .with(csrf())
                .header("Refresh-Token", "refresh-token-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("new-access-token"))
            .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }
}