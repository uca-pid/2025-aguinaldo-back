package com.medibook.api.util;

import com.medibook.api.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ErrorResponseUtil {

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

    public static ResponseEntity<ErrorResponseDTO> createNotFoundResponse(String message, String requestUri) {
        return createErrorResponse("NOT_FOUND", message, HttpStatus.NOT_FOUND, requestUri);
    }

    public static ResponseEntity<ErrorResponseDTO> createBadRequestResponse(String message, String requestUri) {
        return createErrorResponse("BAD_REQUEST", message, HttpStatus.BAD_REQUEST, requestUri);
    }

    public static ResponseEntity<ErrorResponseDTO> createInternalServerErrorResponse(String message, String requestUri) {
        return createErrorResponse("INTERNAL_SERVER_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR, requestUri);
    }

    public static ResponseEntity<ErrorResponseDTO> createDatabaseErrorResponse(String requestUri) {
        return createErrorResponse(
            "DATABASE_ERROR", 
            "Database operation failed", 
            HttpStatus.INTERNAL_SERVER_ERROR, 
            requestUri
        );
    }

    public static ResponseEntity<ErrorResponseDTO> createInvalidUserTypeResponse(String message, String requestUri) {
        return createErrorResponse("INVALID_USER_TYPE", message, HttpStatus.BAD_REQUEST, requestUri);
    }

    public static ResponseEntity<ErrorResponseDTO> createInvalidStatusResponse(String message, String requestUri) {
        return createErrorResponse("INVALID_STATUS", message, HttpStatus.BAD_REQUEST, requestUri);
    }

    public static ResponseEntity<ErrorResponseDTO> createDoctorNotFoundResponse(String message, String requestUri) {
        return createErrorResponse("DOCTOR_NOT_FOUND", message, HttpStatus.NOT_FOUND, requestUri);
    }
}