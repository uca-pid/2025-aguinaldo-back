package com.medibook.api.util;

import com.medibook.api.dto.ErrorResponseDTO;
import com.medibook.api.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Utility class for handling authorization checks and creating appropriate error responses
 */
public class AuthorizationUtil {

    /**
     * Validates if the user has admin role
     */
    public static boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRole());
    }

    /**
     * Validates if the user has doctor role
     */
    public static boolean isDoctor(User user) {
        return user != null && "DOCTOR".equals(user.getRole());
    }

    /**
     * Validates if the user has patient role
     */
    public static boolean isPatient(User user) {
        return user != null && "PATIENT".equals(user.getRole());
    }

    /**
     * Creates an access denied response for admin-only endpoints
     */
    public static ResponseEntity<ErrorResponseDTO> createAdminAccessDeniedResponse(String requestUri) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
            "ACCESS_DENIED", 
            "Only administrators can access this endpoint", 
            HttpStatus.FORBIDDEN.value(),
            requestUri
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Creates an access denied response for doctor-only endpoints
     */
    public static ResponseEntity<ErrorResponseDTO> createDoctorAccessDeniedResponse(String requestUri) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
            "ACCESS_DENIED", 
            "Only doctors can access this endpoint", 
            HttpStatus.FORBIDDEN.value(),
            requestUri
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Creates an access denied response for patient-only endpoints
     */
    public static ResponseEntity<ErrorResponseDTO> createPatientAccessDeniedResponse(String requestUri) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
            "ACCESS_DENIED", 
            "Only patients can access this endpoint", 
            HttpStatus.FORBIDDEN.value(),
            requestUri
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Validates ownership - checks if the user can access a resource that belongs to them
     */
    public static boolean hasOwnership(User user, UUID resourceOwnerId) {
        return user != null && user.getId() != null && user.getId().equals(resourceOwnerId);
    }

    /**
     * Creates an access denied response for ownership validation failures
     */
    public static ResponseEntity<Object> createOwnershipAccessDeniedResponse(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
    }

    /**
     * Creates an invalid role response
     */
    public static ResponseEntity<Object> createInvalidRoleResponse() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid user role");
    }
}