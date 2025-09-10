package com.medibook.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.api.dto.RegisterRequestDTO;
import com.medibook.api.dto.RegisterResponseDTO;
import com.medibook.api.service.AuthService;
import com.medibook.api.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
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

        when(authService.registerPatient(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.email").value(request.email()))
            .andExpect(jsonPath("$.role").value("PATIENT"));
    }

    @Test
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

        when(authService.registerDoctor(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register/doctor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.email").value(request.email()))
            .andExpect(jsonPath("$.role").value("DOCTOR"));
    }

    @Test
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    @Test
    void whenRegisterWithExistingEmail_thenBadRequest() throws Exception {
        // Arrange
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

        when(authService.registerPatient(any()))
            .thenThrow(new IllegalArgumentException("Email already registered"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Email already registered"));
    }

    @Test
    void whenRegisterDoctorWithoutLicense_thenBadRequest() throws Exception {
        // Arrange
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

        when(authService.registerDoctor(any()))
            .thenThrow(new IllegalArgumentException("Medical license and specialty are required for doctors"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register/doctor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Medical license and specialty are required for doctors"));
    }
}