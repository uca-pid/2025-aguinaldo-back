package com.medibook.api.controller;

import com.medibook.api.entity.User;
import com.medibook.api.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerDeactivationTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ProfileController profileController;

    private User authenticatedUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        authenticatedUser = new User();
        authenticatedUser.setId(userId);
        authenticatedUser.setEmail("test@example.com");
        authenticatedUser.setName("Test");
        authenticatedUser.setSurname("User");
        authenticatedUser.setStatus("ACTIVE");
    }

    @Test
    void deactivateMyAccount_Success_ShouldReturnOkWithMessage() {
        when(request.getAttribute("authenticatedUser")).thenReturn(authenticatedUser);
        doNothing().when(profileService).deactivateUser(userId);

        ResponseEntity<?> response = profileController.deactivateMyAccount(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Account deactivated successfully", responseBody.get("message"));
        assertEquals("DISABLED", responseBody.get("status"));
        
        verify(profileService).deactivateUser(userId);
    }

    @Test
    void deactivateMyAccount_ServiceThrowsException_ShouldReturnError() {
        when(request.getAttribute("authenticatedUser")).thenReturn(authenticatedUser);
        when(request.getRequestURI()).thenReturn("/api/profile/me/deactivate");
        doThrow(new RuntimeException("Database error")).when(profileService).deactivateUser(userId);

        ResponseEntity<?> response = profileController.deactivateMyAccount(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(profileService).deactivateUser(userId);
    }

    @Test
    void deactivateMyAccount_NoAuthenticatedUser_ShouldHandleGracefully() {
        when(request.getAttribute("authenticatedUser")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/profile/me/deactivate");

        ResponseEntity<?> response = profileController.deactivateMyAccount(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(profileService, never()).deactivateUser(any());
    }
}