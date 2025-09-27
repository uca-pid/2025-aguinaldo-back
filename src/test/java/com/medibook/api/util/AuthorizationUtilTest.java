package com.medibook.api.util;

import com.medibook.api.dto.ErrorResponseDTO;
import com.medibook.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationUtilTest {

    private User patientUser;
    private User doctorUser;
    private User adminUser;
    private UUID resourceOwnerId;

    @BeforeEach
    void setUp() {
        resourceOwnerId = UUID.randomUUID();
        patientUser = createUser("patient@test.com", 12345678L, "PATIENT", resourceOwnerId);
        doctorUser = createUser("doctor@test.com", 87654321L, "DOCTOR", UUID.randomUUID());
        adminUser = createUser("admin@test.com", 11111111L, "ADMIN", UUID.randomUUID());
    }

    private User createUser(String email, Long dni, String role, UUID id) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setDni(dni);
        user.setPasswordHash("hashedPassword");
        user.setName("Test");
        user.setSurname("User");
        user.setPhone("1234567890");
        user.setBirthdate(LocalDate.of(1990, 1, 1));
        user.setGender("OTHER");
        user.setEmailVerified(true);
        user.setStatus("ACTIVE");
        user.setRole(role);
        return user;
    }

    @Test
    void isPatient_PatientUser_ReturnsTrue() {
        assertTrue(AuthorizationUtil.isPatient(patientUser));
    }

    @Test
    void isPatient_DoctorUser_ReturnsFalse() {
        assertFalse(AuthorizationUtil.isPatient(doctorUser));
    }

    @Test
    void isPatient_AdminUser_ReturnsFalse() {
        assertFalse(AuthorizationUtil.isPatient(adminUser));
    }

    @Test
    void isPatient_NullUser_ReturnsFalse() {
        assertFalse(AuthorizationUtil.isPatient(null));
    }

    @Test
    void isDoctor_DoctorUser_ReturnsTrue() {
        assertTrue(AuthorizationUtil.isDoctor(doctorUser));
    }

    @Test
    void isDoctor_PatientUser_ReturnsFalse() {
        assertFalse(AuthorizationUtil.isDoctor(patientUser));
    }

    @Test
    void isDoctor_AdminUser_ReturnsFalse() {
        assertFalse(AuthorizationUtil.isDoctor(adminUser));
    }

    @Test
    void isDoctor_NullUser_ReturnsFalse() {
        assertFalse(AuthorizationUtil.isDoctor(null));
    }

    @Test
    void isAdmin_AdminUser_ReturnsTrue() {
        assertTrue(AuthorizationUtil.isAdmin(adminUser));
    }

    @Test
    void isAdmin_PatientUser_ReturnsFalse() {
        assertFalse(AuthorizationUtil.isAdmin(patientUser));
    }

    @Test
    void isAdmin_DoctorUser_ReturnsFalse() {
        assertFalse(AuthorizationUtil.isAdmin(doctorUser));
    }

    @Test
    void isAdmin_NullUser_ReturnsFalse() {
        assertFalse(AuthorizationUtil.isAdmin(null));
    }

    @Test
    void hasOwnership_UserOwnsResource_ReturnsTrue() {
        assertTrue(AuthorizationUtil.hasOwnership(patientUser, resourceOwnerId));
    }

    @Test
    void hasOwnership_UserDoesNotOwnResource_ReturnsFalse() {
        assertFalse(AuthorizationUtil.hasOwnership(doctorUser, resourceOwnerId));
    }

    @Test
    void hasOwnership_NullUser_ReturnsFalse() {
        assertFalse(AuthorizationUtil.hasOwnership(null, resourceOwnerId));
    }

    @Test
    void hasOwnership_NullResourceId_ReturnsFalse() {
        assertFalse(AuthorizationUtil.hasOwnership(patientUser, null));
    }

    @Test
    void hasOwnership_NullUserId_ReturnsFalse() {
        User userWithNullId = createUser("user@test.com", 12345678L, "PATIENT", null);
        userWithNullId.setId(null); // Explicitly set ID to null
        assertFalse(AuthorizationUtil.hasOwnership(userWithNullId, resourceOwnerId));
    }

    @Test
    void constructor_CanBeInstantiated() {
        // Test constructor coverage for utility class
        AuthorizationUtil authUtil = new AuthorizationUtil();
        assertNotNull(authUtil);
    }

    @Test
    void createAdminAccessDeniedResponse_ReturnsCorrectFormat() {
        String requestUri = "/api/admin/users";
        ResponseEntity<ErrorResponseDTO> response = AuthorizationUtil.createAdminAccessDeniedResponse(requestUri);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponseDTO error = response.getBody();
        assertNotNull(error);
        assertEquals("ACCESS_DENIED", error.error());
        assertEquals("Only administrators can access this endpoint", error.message());
        assertEquals(HttpStatus.FORBIDDEN.value(), error.status());
        assertEquals(requestUri, error.path());
    }

    @Test
    void createDoctorAccessDeniedResponse_ReturnsCorrectFormat() {
        String requestUri = "/api/doctor/availability";
        ResponseEntity<ErrorResponseDTO> response = AuthorizationUtil.createDoctorAccessDeniedResponse(requestUri);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponseDTO error = response.getBody();
        assertNotNull(error);
        assertEquals("ACCESS_DENIED", error.error());
        assertEquals("Only doctors can access this endpoint", error.message());
        assertEquals(HttpStatus.FORBIDDEN.value(), error.status());
        assertEquals(requestUri, error.path());
    }

    @Test
    void createPatientAccessDeniedResponse_ReturnsCorrectFormat() {
        String requestUri = "/api/patient/profile";
        ResponseEntity<ErrorResponseDTO> response = AuthorizationUtil.createPatientAccessDeniedResponse(requestUri);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponseDTO error = response.getBody();
        assertNotNull(error);
        assertEquals("ACCESS_DENIED", error.error());
        assertEquals("Only patients can access this endpoint", error.message());
        assertEquals(HttpStatus.FORBIDDEN.value(), error.status());
        assertEquals(requestUri, error.path());
    }

    @Test
    void createOwnershipAccessDeniedResponse_ReturnsCorrectFormat() {
        String message = "You can only access your own resources";
        ResponseEntity<Object> response = AuthorizationUtil.createOwnershipAccessDeniedResponse(message);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    void createInvalidRoleResponse_ReturnsCorrectFormat() {
        ResponseEntity<Object> response = AuthorizationUtil.createInvalidRoleResponse();

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Invalid user role", response.getBody());
    }
}
