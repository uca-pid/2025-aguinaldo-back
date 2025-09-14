package com.medibook.api.util;

import com.medibook.api.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for creating standardized error responses
 */
public class ErrorResponseUtil {

    /**
     * Creates a standard error response with the given parameters
     */
    public static ResponseEntity<ErrorResponseDTO> createErrorResponse(
            String errorCode, 
            String message, 
            HttpStatus status, 
            String requestUri) {
        
        ErrorResponseDTO error = ErrorResponseDTO.of(
            errorCode, 
            message, 
            status.value(),
            requestUri
        );
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Creates a not found error response
     */
    public static ResponseEntity<ErrorResponseDTO> createNotFoundResponse(String message, String requestUri) {
        return createErrorResponse("NOT_FOUND", message, HttpStatus.NOT_FOUND, requestUri);
    }

    /**
     * Creates a bad request error response
     */
    public static ResponseEntity<ErrorResponseDTO> createBadRequestResponse(String message, String requestUri) {
        return createErrorResponse("BAD_REQUEST", message, HttpStatus.BAD_REQUEST, requestUri);
    }

    /**
     * Creates an internal server error response
     */
    public static ResponseEntity<ErrorResponseDTO> createInternalServerErrorResponse(String message, String requestUri) {
        return createErrorResponse("INTERNAL_SERVER_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR, requestUri);
    }

    /**
     * Creates a database error response
     */
    public static ResponseEntity<ErrorResponseDTO> createDatabaseErrorResponse(String requestUri) {
        return createErrorResponse(
            "DATABASE_ERROR", 
            "Database operation failed", 
            HttpStatus.INTERNAL_SERVER_ERROR, 
            requestUri
        );
    }

    /**
     * Creates an invalid user type error response
     */
    public static ResponseEntity<ErrorResponseDTO> createInvalidUserTypeResponse(String message, String requestUri) {
        return createErrorResponse("INVALID_USER_TYPE", message, HttpStatus.BAD_REQUEST, requestUri);
    }

    /**
     * Creates an invalid status error response
     */
    public static ResponseEntity<ErrorResponseDTO> createInvalidStatusResponse(String message, String requestUri) {
        return createErrorResponse("INVALID_STATUS", message, HttpStatus.BAD_REQUEST, requestUri);
    }

    /**
     * Creates a doctor not found error response
     */
    public static ResponseEntity<ErrorResponseDTO> createDoctorNotFoundResponse(String message, String requestUri) {
        return createErrorResponse("DOCTOR_NOT_FOUND", message, HttpStatus.NOT_FOUND, requestUri);
    }
}