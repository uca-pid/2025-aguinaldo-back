package com.medibook.api.util;

import com.medibook.api.dto.ErrorResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseUtilTest {

    private final String requestUri = "/api/test";

    @Test
    void createErrorResponse_ValidParameters_ReturnsCorrectResponse() {
        String errorCode = "TEST_ERROR";
        String message = "Test error message";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseEntity<ErrorResponseDTO> response = ErrorResponseUtil.createErrorResponse(
            errorCode, message, status, requestUri
        );

        assertNotNull(response);
        assertEquals(status, response.getStatusCode());
        ErrorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals(errorCode, body.error());
        assertEquals(message, body.message());
        assertEquals(status.value(), body.status());
        assertEquals(requestUri, body.path());
        assertNotNull(body.timestamp());
    }

    @Test
    void createNotFoundResponse_ValidMessage_ReturnsNotFoundResponse() {
        String message = "Resource not found";

        ResponseEntity<ErrorResponseDTO> response = ErrorResponseUtil.createNotFoundResponse(message, requestUri);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("NOT_FOUND", body.error());
        assertEquals(message, body.message());
        assertEquals(404, body.status());
        assertEquals(requestUri, body.path());
    }

    @Test
    void createBadRequestResponse_ValidMessage_ReturnsBadRequestResponse() {
        String message = "Invalid request";

        ResponseEntity<ErrorResponseDTO> response = ErrorResponseUtil.createBadRequestResponse(message, requestUri);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("BAD_REQUEST", body.error());
        assertEquals(message, body.message());
        assertEquals(400, body.status());
        assertEquals(requestUri, body.path());
    }

    @Test
    void createInternalServerErrorResponse_ValidMessage_ReturnsInternalServerErrorResponse() {
        String message = "Internal server error";

        ResponseEntity<ErrorResponseDTO> response = ErrorResponseUtil.createInternalServerErrorResponse(message, requestUri);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("INTERNAL_SERVER_ERROR", body.error());
        assertEquals(message, body.message());
        assertEquals(500, body.status());
        assertEquals(requestUri, body.path());
    }

    @Test
    void createDatabaseErrorResponse_ValidUri_ReturnsDatabaseErrorResponse() {
        ResponseEntity<ErrorResponseDTO> response = ErrorResponseUtil.createDatabaseErrorResponse(requestUri);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("DATABASE_ERROR", body.error());
        assertEquals("Database operation failed", body.message());
        assertEquals(500, body.status());
        assertEquals(requestUri, body.path());
    }

    @Test
    void createInvalidUserTypeResponse_ValidMessage_ReturnsInvalidUserTypeResponse() {
        String message = "Invalid user type";

        ResponseEntity<ErrorResponseDTO> response = ErrorResponseUtil.createInvalidUserTypeResponse(message, requestUri);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("INVALID_USER_TYPE", body.error());
        assertEquals(message, body.message());
        assertEquals(400, body.status());
        assertEquals(requestUri, body.path());
    }

    @Test
    void createInvalidStatusResponse_ValidMessage_ReturnsInvalidStatusResponse() {
        String message = "Invalid status";

        ResponseEntity<ErrorResponseDTO> response = ErrorResponseUtil.createInvalidStatusResponse(message, requestUri);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("INVALID_STATUS", body.error());
        assertEquals(message, body.message());
        assertEquals(400, body.status());
        assertEquals(requestUri, body.path());
    }
}