package com.medibook.api.service;

import com.medibook.api.dto.RegisterRequestDTO;
import com.medibook.api.dto.RegisterResponseDTO;
import com.medibook.api.dto.SignInRequestDTO;
import com.medibook.api.dto.SignInResponseDTO;
import com.medibook.api.entity.RefreshToken;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.AuthMapper;
import com.medibook.api.mapper.UserMapper;
import com.medibook.api.repository.RefreshTokenRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuthMapper authMapper;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, refreshTokenRepository, passwordEncoder, userMapper, authMapper);
    }

    @Test
    void whenRegisterPatient_thenSuccess() {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "patient@example.com",
            12345678L,
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
        when(userRepository.existsByDni(request.dni())).thenReturn(false);
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
        verify(userRepository).existsByDni(request.dni());
        verify(passwordEncoder).encode(request.password());
        verify(userMapper).toUser(request, "PATIENT", "hashed_password");
        verify(userRepository).save(user);
        verify(userMapper).toRegisterResponse(user);
    }

    @Test
    void whenRegisterPatientWithExistingEmail_thenThrowException() {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "existing@example.com",
            12345678L,
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
            87654321L,
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
        when(userRepository.existsByDni(request.dni())).thenReturn(false);
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
        verify(userRepository).existsByDni(request.dni());
        verify(passwordEncoder).encode(request.password());
        verify(userMapper).toUser(request, "DOCTOR", "hashed_password");
        verify(userRepository).save(user);
        verify(userMapper).toRegisterResponse(user);
    }

    @Test
    void whenRegisterDoctorWithoutLicense_thenThrowException() {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "doctor@example.com",
            87654321L,
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
        when(userRepository.existsByDni(request.dni())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.registerDoctor(request)
        );

        assertEquals("Medical license and specialty are required for doctors", exception.getMessage());
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository).existsByDni(request.dni());
        verifyNoMoreInteractions(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void whenRegisterPatientWithExistingDni_thenThrowException() {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "patient@example.com",
            12345678L,
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

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByDni(request.dni())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.registerPatient(request)
        );

        assertEquals("DNI already registered", exception.getMessage());
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository).existsByDni(request.dni());
        verifyNoMoreInteractions(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void whenSignInWithValidCredentials_thenSuccess() {
        SignInRequestDTO request = new SignInRequestDTO("user@example.com", "password123");
        
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPasswordHash("hashed_password");
        user.setStatus("ACTIVE");

        SignInResponseDTO expectedResponse = new SignInResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getSurname(),
            user.getRole(),
            "access-token",
            "refresh-token"
        );

        when(userRepository.findByEmailAndStatus(request.email(), "ACTIVE")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(authMapper.toSignInResponse(eq(user), anyString(), anyString())).thenReturn(expectedResponse);

        SignInResponseDTO response = authService.signIn(request);

        assertNotNull(response);
        assertEquals(expectedResponse.id(), response.id());
        assertEquals(expectedResponse.email(), response.email());
        assertEquals(expectedResponse.accessToken(), response.accessToken());

        verify(userRepository).findByEmailAndStatus(request.email(), "ACTIVE");
        verify(passwordEncoder).matches(request.password(), user.getPasswordHash());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(authMapper).toSignInResponse(eq(user), anyString(), anyString());
    }

    @Test
    void whenSignInWithInvalidEmail_thenThrowException() {
        SignInRequestDTO request = new SignInRequestDTO("invalid@example.com", "password123");

        when(userRepository.findByEmailAndStatus(request.email(), "ACTIVE")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.signIn(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmailAndStatus(request.email(), "ACTIVE");
        verifyNoMoreInteractions(passwordEncoder, refreshTokenRepository, authMapper);
    }

    @Test
    void whenSignInWithInvalidPassword_thenThrowException() {
        SignInRequestDTO request = new SignInRequestDTO("user@example.com", "wrongpassword");
        
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash("hashed_password");
        user.setStatus("ACTIVE");

        when(userRepository.findByEmailAndStatus(request.email(), "ACTIVE")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.signIn(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmailAndStatus(request.email(), "ACTIVE");
        verify(passwordEncoder).matches(request.password(), user.getPasswordHash());
        verifyNoMoreInteractions(refreshTokenRepository, authMapper);
    }

    @Test
    void whenSignOut_thenRevokeToken() {
        String refreshTokenHash = "token-hash";

        authService.signOut(refreshTokenHash);

        verify(refreshTokenRepository).revokeTokenByHash(eq(refreshTokenHash), any(ZonedDateTime.class));
    }

    @Test
    void whenRefreshTokenWithValidToken_thenSuccess() {
        String refreshTokenHash = "valid-token-hash";
        
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setName("John");
        user.setSurname("Doe");
        user.setRole("PATIENT");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(refreshTokenHash);
        refreshToken.setExpiresAt(ZonedDateTime.now().plusDays(30));
        refreshToken.setCreatedAt(ZonedDateTime.now());

        SignInResponseDTO expectedResponse = new SignInResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getSurname(),
            user.getRole(),
            "new-access-token",
            "new-refresh-token"
        );

        when(refreshTokenRepository.findByTokenHash(refreshTokenHash)).thenReturn(Optional.of(refreshToken));
        when(authMapper.toSignInResponse(eq(user), anyString(), anyString())).thenReturn(expectedResponse);

        SignInResponseDTO response = authService.refreshToken(refreshTokenHash);

        assertNotNull(response);
        assertEquals(expectedResponse.id(), response.id());
        assertEquals(expectedResponse.email(), response.email());

        verify(refreshTokenRepository).findByTokenHash(refreshTokenHash);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(refreshTokenRepository).revokeTokenByHash(eq(refreshTokenHash), any(ZonedDateTime.class));
        verify(authMapper).toSignInResponse(eq(user), anyString(), anyString());
    }

    @Test
    void whenRefreshTokenWithInvalidToken_thenThrowException() {
        String invalidTokenHash = "invalid-token-hash";

        when(refreshTokenRepository.findByTokenHash(invalidTokenHash)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.refreshToken(invalidTokenHash)
        );

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(refreshTokenRepository).findByTokenHash(invalidTokenHash);
        verifyNoMoreInteractions(refreshTokenRepository, authMapper);
    }

    @Test
    void whenRefreshTokenWithExpiredToken_thenThrowException() {
        String expiredTokenHash = "expired-token-hash";
        
        User user = new User();
        user.setEmail("user@example.com");

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setUser(user);
        expiredToken.setTokenHash(expiredTokenHash);
        expiredToken.setExpiresAt(ZonedDateTime.now().minusDays(1)); // Expired
        expiredToken.setCreatedAt(ZonedDateTime.now().minusDays(31));

        when(refreshTokenRepository.findByTokenHash(expiredTokenHash)).thenReturn(Optional.of(expiredToken));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.refreshToken(expiredTokenHash)
        );

        assertEquals("Refresh token is expired or revoked", exception.getMessage());
        verify(refreshTokenRepository).findByTokenHash(expiredTokenHash);
        verifyNoMoreInteractions(refreshTokenRepository, authMapper);
    }
}
