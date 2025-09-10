package com.medibook.api.service;

import com.medibook.api.dto.RegisterRequestDTO;
import com.medibook.api.dto.RegisterResponseDTO;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.UserMapper;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void whenRegisterPatient_thenSuccess() {
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

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setRole("PATIENT");

        RegisterResponseDTO expectedResponse = new RegisterResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getSurname(),
            user.getRole()
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed_password");
        when(userMapper.toUser(request, "PATIENT", "hashed_password")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toRegisterResponse(user)).thenReturn(expectedResponse);

        RegisterResponseDTO response = authService.registerPatient(request);

        assertNotNull(response);
        assertEquals(expectedResponse.id(), response.id());
        assertEquals(expectedResponse.email(), response.email());
        assertEquals(expectedResponse.role(), response.role());

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userMapper).toUser(request, "PATIENT", "hashed_password");
        verify(userRepository).save(user);
        verify(userMapper).toRegisterResponse(user);
    }

    @Test
    void whenRegisterPatientWithExistingEmail_thenThrowException() {
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

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.registerPatient(request)
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository).existsByEmail(request.email());
        verifyNoMoreInteractions(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void whenRegisterDoctor_thenSuccess() {
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

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setRole("DOCTOR");

        RegisterResponseDTO expectedResponse = new RegisterResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getSurname(),
            user.getRole()
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed_password");
        when(userMapper.toUser(request, "DOCTOR", "hashed_password")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toRegisterResponse(user)).thenReturn(expectedResponse);

        RegisterResponseDTO response = authService.registerDoctor(request);

        assertNotNull(response);
        assertEquals(expectedResponse.id(), response.id());
        assertEquals(expectedResponse.email(), response.email());
        assertEquals(expectedResponse.role(), response.role());

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userMapper).toUser(request, "DOCTOR", "hashed_password");
        verify(userRepository).save(user);
        verify(userMapper).toRegisterResponse(user);
    }

    @Test
    void whenRegisterDoctorWithoutLicense_thenThrowException() {
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

        when(userRepository.existsByEmail(request.email())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.registerDoctor(request)
        );

        assertEquals("Medical license and specialty are required for doctors", exception.getMessage());
        verify(userRepository).existsByEmail(request.email());
        verifyNoMoreInteractions(userRepository, passwordEncoder, userMapper);
    }
}
