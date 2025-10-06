package com.medibook.api.service;

import com.medibook.api.dto.Auth.SignInRequestDTO;
import com.medibook.api.entity.User;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceDeactivatedUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User disabledUser;
    private SignInRequestDTO signInRequest;

    @BeforeEach
    void setUp() {
        disabledUser = new User();
        disabledUser.setId(UUID.randomUUID());
        disabledUser.setEmail("disabled@example.com");
        disabledUser.setPasswordHash("hashedPassword");
        disabledUser.setStatus("DISABLED");
        disabledUser.setRole("PATIENT"); // Agregamos el rol

        signInRequest = new SignInRequestDTO("disabled@example.com", "password123");
    }

    @Test
    void signIn_DisabledUser_ShouldThrowException() {
        when(userRepository.findByEmail("disabled@example.com"))
            .thenReturn(Optional.of(disabledUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signIn(signInRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail("disabled@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void signIn_DisabledUserWithCorrectPassword_ShouldStillFail() {
        when(userRepository.findByEmail("disabled@example.com"))
            .thenReturn(Optional.of(disabledUser));

        assertThrows(IllegalArgumentException.class, () -> {
            authService.signIn(signInRequest);
        });

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void findByEmailAndStatus_DisabledUser_ShouldReturnEmpty() {
        when(userRepository.findByEmailAndStatus("disabled@example.com", "ACTIVE"))
            .thenReturn(Optional.empty());

        Optional<User> result = userRepository.findByEmailAndStatus("disabled@example.com", "ACTIVE");

        assertTrue(result.isEmpty());
        verify(userRepository).findByEmailAndStatus("disabled@example.com", "ACTIVE");
    }
}