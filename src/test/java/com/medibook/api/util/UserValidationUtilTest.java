package com.medibook.api.util;

import com.medibook.api.dto.ErrorResponseDTO;
import com.medibook.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationUtilTest {

    private User doctorUser;
    private User patientUser;
    private String requestUri;

    @Test
    void constructor_CanBeInstantiated() {
        // Test constructor coverage for utility class
        UserValidationUtil util = new UserValidationUtil();
        assertNotNull(util);
    }

    @BeforeEach
    void setUp() {
        doctorUser = new User();
        doctorUser.setId(UUID.randomUUID());
        doctorUser.setRole("DOCTOR");
        doctorUser.setStatus("PENDING");

        patientUser = new User();
        patientUser.setId(UUID.randomUUID());
        patientUser.setRole("PATIENT");
        patientUser.setStatus("ACTIVE");

        requestUri = "/api/test";
    }

    @Test
    void validateDoctorForApproval_ValidDoctor_ReturnsNull() {
        ResponseEntity<ErrorResponseDTO> result = UserValidationUtil.validateDoctorForApproval(doctorUser, requestUri);
        assertNull(result);
    }

    @Test
    void validateDoctorForApproval_NotDoctor_ReturnsError() {
        ResponseEntity<ErrorResponseDTO> result = UserValidationUtil.validateDoctorForApproval(patientUser, requestUri);
        
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ErrorResponseDTO error = result.getBody();
        assertNotNull(error);
        assertEquals("INVALID_USER_TYPE", error.error());
        assertTrue(error.message().contains("not a doctor"));
    }

    @Test
    void validateDoctorForApproval_NotPendingStatus_ReturnsError() {
        doctorUser.setStatus("ACTIVE");
        
        ResponseEntity<ErrorResponseDTO> result = UserValidationUtil.validateDoctorForApproval(doctorUser, requestUri);
        
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ErrorResponseDTO error = result.getBody();
        assertNotNull(error);
        assertEquals("INVALID_STATUS", error.error());
        assertTrue(error.message().contains("not in pending status"));
    }

    @Test
    void validateDoctorExists_ValidDoctor_ReturnsNull() {
        ResponseEntity<ErrorResponseDTO> result = UserValidationUtil.validateDoctorExists(doctorUser, requestUri);
        assertNull(result);
    }

    @Test
    void validateDoctorExists_NullDoctor_ReturnsError() {
        ResponseEntity<ErrorResponseDTO> result = UserValidationUtil.validateDoctorExists(null, requestUri);
        
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        ErrorResponseDTO error = result.getBody();
        assertNotNull(error);
        assertEquals("DOCTOR_NOT_FOUND", error.error());
        assertTrue(error.message().contains("not found"));
    }

    @Test
    void validateDoctorExists_NotDoctor_ReturnsError() {
        ResponseEntity<ErrorResponseDTO> result = UserValidationUtil.validateDoctorExists(patientUser, requestUri);
        
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ErrorResponseDTO error = result.getBody();
        assertNotNull(error);
        assertEquals("INVALID_USER_TYPE", error.error());
        assertTrue(error.message().contains("not a doctor"));
    }

    @Test
    void validateDoctorForStatusChange_ValidDoctor_ReturnsNull() {
        ResponseEntity<ErrorResponseDTO> result = UserValidationUtil.validateDoctorForStatusChange(doctorUser, requestUri);
        assertNull(result);
    }

    @Test
    void validateDoctorForStatusChange_NullDoctor_ReturnsError() {
        ResponseEntity<ErrorResponseDTO> result = UserValidationUtil.validateDoctorForStatusChange(null, requestUri);
        
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void validateDoctorForStatusChange_NotDoctor_ReturnsError() {
        ResponseEntity<ErrorResponseDTO> result = UserValidationUtil.validateDoctorForStatusChange(patientUser, requestUri);
        
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ErrorResponseDTO error = result.getBody();
        assertNotNull(error);
        assertEquals("INVALID_USER_TYPE", error.error());
    }

    @Test
    void validateDoctorForStatusChange_NotPendingStatus_ReturnsError() {
        doctorUser.setStatus("ACTIVE");
        
        ResponseEntity<ErrorResponseDTO> result = UserValidationUtil.validateDoctorForStatusChange(doctorUser, requestUri);
        
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ErrorResponseDTO error = result.getBody();
        assertNotNull(error);
        assertEquals("INVALID_STATUS", error.error());
    }
}