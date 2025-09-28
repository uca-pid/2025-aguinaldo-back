package com.medibook.api.repository;

import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.TurnModifyRequest;
import com.medibook.api.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TurnModifyRequestRepositoryTest {

    @Autowired
    private TurnModifyRequestRepository turnModifyRequestRepository;

    @Autowired
    private EntityManager entityManager;

    private User patientUser;
    private User doctorUser;
    private TurnAssigned turnAssigned;
    private TurnModifyRequest modifyRequest1;
    private TurnModifyRequest modifyRequest2;

    @BeforeEach
    void setUp() {
        turnModifyRequestRepository.deleteAll();
        
        patientUser = createUser("patient@test.com", 12345678L, "PATIENT", "ACTIVE");
        doctorUser = createUser("doctor@test.com", 87654321L, "DOCTOR", "APPROVED");
        
        entityManager.persist(patientUser);
        entityManager.persist(doctorUser);
        entityManager.flush();
        
        turnAssigned = createTurnAssigned(doctorUser, patientUser, 
                OffsetDateTime.now().plusDays(1), "SCHEDULED");
        entityManager.persist(turnAssigned);
        entityManager.flush();
        
        modifyRequest1 = createModifyRequest(turnAssigned, patientUser, doctorUser, 
                "PENDING", OffsetDateTime.now().plusDays(2));
        modifyRequest2 = createModifyRequest(turnAssigned, patientUser, doctorUser, 
                "APPROVED", OffsetDateTime.now().plusDays(3));
        
        entityManager.persist(modifyRequest1);
        entityManager.persist(modifyRequest2);
        entityManager.flush();
        
        entityManager.clear();
    }

    @Test
    void findByPatient_IdOrderByIdDesc_ShouldReturnRequestsOrderedByIdDesc() {
        List<TurnModifyRequest> results = turnModifyRequestRepository
                .findByPatient_IdOrderByIdDesc(patientUser.getId());
        
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getStatus().equals("PENDING")));
        assertTrue(results.stream().anyMatch(r -> r.getStatus().equals("APPROVED")));
    }

    @Test
    void findByDoctor_IdAndStatusOrderByIdDesc_ShouldReturnFilteredRequestsOrderedByIdDesc() {
        List<TurnModifyRequest> pendingResults = turnModifyRequestRepository
                .findByDoctor_IdAndStatusOrderByIdDesc(doctorUser.getId(), "PENDING");
        
        List<TurnModifyRequest> approvedResults = turnModifyRequestRepository
                .findByDoctor_IdAndStatusOrderByIdDesc(doctorUser.getId(), "APPROVED");
        
        assertNotNull(pendingResults);
        assertEquals(1, pendingResults.size());
        assertEquals("PENDING", pendingResults.get(0).getStatus());
        
        assertNotNull(approvedResults);
        assertEquals(1, approvedResults.size());
        assertEquals("APPROVED", approvedResults.get(0).getStatus());
    }

    @Test
    void findByTurnAssigned_IdAndStatus_ShouldReturnCorrectRequest() {
        Optional<TurnModifyRequest> pendingResult = turnModifyRequestRepository
                .findByTurnAssigned_IdAndStatus(turnAssigned.getId(), "PENDING");
        
        Optional<TurnModifyRequest> approvedResult = turnModifyRequestRepository
                .findByTurnAssigned_IdAndStatus(turnAssigned.getId(), "APPROVED");
        
        Optional<TurnModifyRequest> nonExistentResult = turnModifyRequestRepository
                .findByTurnAssigned_IdAndStatus(turnAssigned.getId(), "REJECTED");
        
        assertTrue(pendingResult.isPresent());
        assertEquals("PENDING", pendingResult.get().getStatus());
        
        assertTrue(approvedResult.isPresent());
        assertEquals("APPROVED", approvedResult.get().getStatus());
        
        assertFalse(nonExistentResult.isPresent());
    }

    @Test
    void findPendingRequestByTurnAndPatient_ShouldReturnPendingRequest() {
        Optional<TurnModifyRequest> result = turnModifyRequestRepository
                .findPendingRequestByTurnAndPatient(turnAssigned.getId(), patientUser.getId());
        
        assertTrue(result.isPresent());
        assertEquals("PENDING", result.get().getStatus());
        assertEquals(turnAssigned.getId(), result.get().getTurnAssigned().getId());
        assertEquals(patientUser.getId(), result.get().getPatient().getId());
    }

    @Test
    void findPendingRequestByTurnAndPatient_WithNonExistentTurn_ShouldReturnEmpty() {
        UUID nonExistentTurnId = UUID.randomUUID();
        
        Optional<TurnModifyRequest> result = turnModifyRequestRepository
                .findPendingRequestByTurnAndPatient(nonExistentTurnId, patientUser.getId());
        
        assertFalse(result.isPresent());
    }

    @Test
    void findPendingRequestByTurnAndPatient_WithDifferentPatient_ShouldReturnEmpty() {
        User otherPatient = createUser("other@test.com", 11111111L, "PATIENT", "ACTIVE");
        entityManager.persist(otherPatient);
        entityManager.flush();
        
        Optional<TurnModifyRequest> result = turnModifyRequestRepository
                .findPendingRequestByTurnAndPatient(turnAssigned.getId(), otherPatient.getId());
        
        assertFalse(result.isPresent());
    }

    private User createUser(String email, Long dni, String role, String status) {
        User user = new User();
        user.setEmail(email);
        user.setDni(dni);
        user.setPasswordHash("hashedPassword");
        user.setName("Test");
        user.setSurname("User");
        user.setPhone("1234567890");
        user.setBirthdate(LocalDate.of(1990, 1, 1));
        user.setGender("MALE");
        user.setEmailVerified(true);
        user.setStatus(status);
        user.setRole(role);
        return user;
    }

    private TurnAssigned createTurnAssigned(User doctor, User patient, 
                                           OffsetDateTime scheduledAt, String status) {
        TurnAssigned turn = new TurnAssigned();
        turn.setDoctor(doctor);
        turn.setPatient(patient);
        turn.setScheduledAt(scheduledAt);
        turn.setStatus(status);
        return turn;
    }

    private TurnModifyRequest createModifyRequest(TurnAssigned turnAssigned, User patient, User doctor,
                                                 String status, OffsetDateTime requestedScheduledAt) {
        return TurnModifyRequest.builder()
                .turnAssigned(turnAssigned)
                .patient(patient)
                .doctor(doctor)
                .currentScheduledAt(turnAssigned.getScheduledAt())
                .requestedScheduledAt(requestedScheduledAt)
                .status(status)
                .build();
    }
}