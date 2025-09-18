package com.medibook.api.service;

import com.medibook.api.entity.User;
import com.medibook.api.mapper.ProfileMapper;
import com.medibook.api.repository.RefreshTokenRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceDeactivationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private ProfileService profileService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setName("Test");
        user.setSurname("User");
        user.setStatus("ACTIVE");
    }

    @Test
    void deactivateUser_UserExists_ShouldDeactivateUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        profileService.deactivateUser(userId);

        assertEquals("DISABLED", user.getStatus());
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(refreshTokenRepository).revokeAllTokensByUserId(eq(userId), any(ZonedDateTime.class));
    }

    @Test
    void deactivateUser_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileService.deactivateUser(userId);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeAllTokensByUserId(any(), any());
    }

    @Test
    void deactivateUser_ShouldRevokeRefreshTokens() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(refreshTokenRepository.revokeAllTokensByUserId(eq(userId), any(ZonedDateTime.class))).thenReturn(2);

        profileService.deactivateUser(userId);

        verify(refreshTokenRepository).revokeAllTokensByUserId(eq(userId), any(ZonedDateTime.class));
    }

    @Test
    void deactivateUser_AlreadyDisabled_ShouldStillWork() {
        user.setStatus("DISABLED");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        profileService.deactivateUser(userId);

        assertEquals("DISABLED", user.getStatus());
        verify(userRepository).save(user);
    }
}