package com.medibook.api.service;

import com.medibook.api.entity.User;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticatedUserService authenticatedUserService;

    private User activeUser;
    private User inactiveUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        activeUser = new User();
        activeUser.setId(userId);
        activeUser.setEmail("test@example.com");
        activeUser.setRole("PATIENT");
        activeUser.setStatus("ACTIVE");

        inactiveUser = new User();
        inactiveUser.setId(userId);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setRole("PATIENT");
        inactiveUser.setStatus("DISABLED");
    }

    @Test
    void validateAccessToken_ValidToken_ReturnsUser() {
        String accessToken = "jwt-token-for-user-" + userId.toString();
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));

        Optional<User> result = authenticatedUserService.validateAccessToken(accessToken);

        assertTrue(result.isPresent());
        assertEquals(activeUser, result.get());
        verify(userRepository).findById(userId);
    }

    @Test
    void validateAccessToken_NullToken_ReturnsEmpty() {
        Optional<User> result = authenticatedUserService.validateAccessToken(null);

        assertFalse(result.isPresent());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void validateAccessToken_EmptyToken_ReturnsEmpty() {
        Optional<User> result = authenticatedUserService.validateAccessToken("");

        assertFalse(result.isPresent());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void validateAccessToken_InvalidTokenFormat_ReturnsEmpty() {
        String invalidToken = "invalid-token-format";

        Optional<User> result = authenticatedUserService.validateAccessToken(invalidToken);

        assertFalse(result.isPresent());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void validateAccessToken_InvalidUUID_ReturnsEmpty() {
        String tokenWithInvalidUUID = "jwt-token-for-user-invalid-uuid";

        Optional<User> result = authenticatedUserService.validateAccessToken(tokenWithInvalidUUID);

        assertFalse(result.isPresent());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void validateAccessToken_UserNotFound_ReturnsEmpty() {
        String accessToken = "jwt-token-for-user-" + userId.toString();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = authenticatedUserService.validateAccessToken(accessToken);

        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void validateAccessToken_UserNotActive_ReturnsEmpty() {
        String accessToken = "jwt-token-for-user-" + userId.toString();
        when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));

        Optional<User> result = authenticatedUserService.validateAccessToken(accessToken);

        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserFromAuthorizationHeader_ValidBearerToken_ReturnsUser() {
        String authorizationHeader = "Bearer jwt-token-for-user-" + userId.toString();
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));

        Optional<User> result = authenticatedUserService.getUserFromAuthorizationHeader(authorizationHeader);

        assertTrue(result.isPresent());
        assertEquals(activeUser, result.get());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserFromAuthorizationHeader_NullHeader_ReturnsEmpty() {
        Optional<User> result = authenticatedUserService.getUserFromAuthorizationHeader(null);

        assertFalse(result.isPresent());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserFromAuthorizationHeader_InvalidBearerFormat_ReturnsEmpty() {
        String invalidHeader = "InvalidBearer jwt-token-for-user-" + userId.toString();

        Optional<User> result = authenticatedUserService.getUserFromAuthorizationHeader(invalidHeader);

        assertFalse(result.isPresent());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserFromAuthorizationHeader_NoBearer_ReturnsEmpty() {
        String headerWithoutBearer = "jwt-token-for-user-" + userId.toString();

        Optional<User> result = authenticatedUserService.getUserFromAuthorizationHeader(headerWithoutBearer);

        assertFalse(result.isPresent());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserFromAuthorizationHeader_InvalidTokenInBearer_ReturnsEmpty() {
        String authorizationHeader = "Bearer invalid-token-format";

        Optional<User> result = authenticatedUserService.getUserFromAuthorizationHeader(authorizationHeader);

        assertFalse(result.isPresent());
        verify(userRepository, never()).findById(any());
    }
}