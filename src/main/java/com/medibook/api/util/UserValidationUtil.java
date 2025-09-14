package com.medibook.api.util;

import com.medibook.api.dto.ErrorResponseDTO;
import com.medibook.api.entity.User;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for user validation operations
 */
public class UserValidationUtil {

    /**
     * Validates if a user is a doctor in pending status
     * Returns null if valid, otherwise returns an error response
     */
    public static ResponseEntity<ErrorResponseDTO> validateDoctorForApproval(User doctor, String requestUri) {
        if (!"DOCTOR".equals(doctor.getRole())) {
            return ErrorResponseUtil.createInvalidUserTypeResponse(
                "User is not a doctor", 
                requestUri
            );
        }

        if (!"PENDING".equals(doctor.getStatus())) {
            return ErrorResponseUtil.createInvalidStatusResponse(
                "Doctor is not in pending status. Current status: " + doctor.getStatus(), 
                requestUri
            );
        }

        return null; // No validation errors
    }

    /**
     * Validates if a user exists and is a doctor
     * Returns null if valid, otherwise returns an error response
     */
    public static ResponseEntity<ErrorResponseDTO> validateDoctorExists(User doctor, String requestUri) {
        if (doctor == null) {
            return ErrorResponseUtil.createDoctorNotFoundResponse(
                "Doctor not found", 
                requestUri
            );
        }

        if (!"DOCTOR".equals(doctor.getRole())) {
            return ErrorResponseUtil.createInvalidUserTypeResponse(
                "User is not a doctor", 
                requestUri
            );
        }

        return null; // No validation errors
    }

    /**
     * Validates if a user can be approved/rejected
     * Combines existence and approval status checks
     */
    public static ResponseEntity<ErrorResponseDTO> validateDoctorForStatusChange(User doctor, String requestUri) {
        ResponseEntity<ErrorResponseDTO> existsValidation = validateDoctorExists(doctor, requestUri);
        if (existsValidation != null) {
            return existsValidation;
        }

        return validateDoctorForApproval(doctor, requestUri);
    }
}