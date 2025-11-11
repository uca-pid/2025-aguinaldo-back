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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeEvaluationTriggerServiceTest {

    @Mock
    private DoctorBadgeService badgeService;

    @Mock
    private DoctorBadgeStatisticsUpdateService statisticsUpdateService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BadgeEvaluationTriggerService triggerService;

    private UUID doctorId;
    private UUID patientId;
    private User doctor;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctor = new User();
        doctor.setId(doctorId);
        doctor.setName("Dr. Test");
        doctor.setSurname("Doctor");
        doctor.setRole("DOCTOR");
        doctor.setStatus("ACTIVE");
    }

    @Test
    void evaluateAfterRating_ValidDoctor_UpdatesStatisticsAndEvaluatesBadges() {
        Integer communicationScore = 4;
        Integer empathyScore = 5;
        Integer punctualityScore = 4;

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        triggerService.evaluateAfterRating(doctorId, communicationScore, empathyScore, punctualityScore);

        verify(userRepository).findById(doctorId);
        verify(statisticsUpdateService).updateAfterRatingAdded(doctorId, communicationScore, empathyScore, punctualityScore);
        verify(statisticsUpdateService).updateProgressAfterRating(doctorId);
        verify(badgeService).evaluateRatingRelatedBadges(doctorId);
    }

    @Test
    void evaluateAfterRating_InvalidDoctorRole_HandlesGracefully() {
        User patient = new User();
        patient.setId(doctorId);
        patient.setRole("PATIENT");

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(patient));

        assertDoesNotThrow(() ->
            triggerService.evaluateAfterRating(doctorId, 4, 5, 4));

        verify(userRepository).findById(doctorId);
        verifyNoInteractions(statisticsUpdateService);
        verifyNoInteractions(badgeService);
    }

    @Test
    void evaluateAfterRating_DoctorNotFound_HandlesGracefully() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
            triggerService.evaluateAfterRating(doctorId, 4, 5, 4));

        verify(userRepository).findById(doctorId);
        verifyNoInteractions(statisticsUpdateService);
        verifyNoInteractions(badgeService);
    }

    @Test
    void evaluateAfterRating_ExceptionInStatisticsUpdate_HandlesGracefully() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        doThrow(new RuntimeException("Statistics update failed"))
            .when(statisticsUpdateService).updateAfterRatingAdded(any(), any(), any(), any());

        assertDoesNotThrow(() ->
            triggerService.evaluateAfterRating(doctorId, 4, 5, 4));

        verify(userRepository).findById(doctorId);
        verify(statisticsUpdateService).updateAfterRatingAdded(doctorId, 4, 5, 4);
        verify(statisticsUpdateService, never()).updateProgressAfterRating(any());
        verify(badgeService, never()).evaluateRatingRelatedBadges(any());
    }

    @Test
    void evaluateAfterTurnCompletion_ValidDoctor_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        triggerService.evaluateAfterTurnCompletion(doctorId, patientId);

        verify(userRepository).findById(doctorId);
        verify(statisticsUpdateService).updateAfterTurnCompleted(doctorId, patientId);
        verify(statisticsUpdateService).updateProgressAfterTurnCompletion(doctorId);
        verify(badgeService).evaluateTurnCompletionRelatedBadges(doctorId);
    }

    @Test
    void evaluateAfterTurnCompletion_InvalidDoctorRole_HandlesGracefully() {
        User patient = new User();
        patient.setId(doctorId);
        patient.setRole("PATIENT");

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(patient));

        assertDoesNotThrow(() ->
            triggerService.evaluateAfterTurnCompletion(doctorId, patientId));

        verify(userRepository).findById(doctorId);
        verifyNoInteractions(statisticsUpdateService);
        verifyNoInteractions(badgeService);
    }

    @Test
    void evaluateAfterMedicalHistoryDocumented_ValidDoctor_UpdatesStatisticsAndEvaluatesBadges() {
        String content = "Patient shows symptoms of diabetes";

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        triggerService.evaluateAfterMedicalHistoryDocumented(doctorId, content);

        verify(userRepository).findById(doctorId);
        verify(statisticsUpdateService).updateAfterMedicalHistoryDocumented(doctorId, content);
        verify(statisticsUpdateService).updateProgressAfterMedicalHistory(doctorId);
        verify(badgeService).evaluateDocumentationRelatedBadges(doctorId);
    }

    @Test
    void evaluateAfterMedicalHistoryDocumented_NullContent_HandlesGracefully() {
        String content = null;

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        triggerService.evaluateAfterMedicalHistoryDocumented(doctorId, content);

        verify(userRepository).findById(doctorId);
        verify(statisticsUpdateService).updateAfterMedicalHistoryDocumented(doctorId, content);
        verify(statisticsUpdateService).updateProgressAfterMedicalHistory(doctorId);
        verify(badgeService).evaluateDocumentationRelatedBadges(doctorId);
    }

    @Test
    void evaluateAfterModifyRequestHandled_ValidDoctor_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        triggerService.evaluateAfterModifyRequestHandled(doctorId);

        verify(userRepository).findById(doctorId);
        verify(statisticsUpdateService).updateAfterModifyRequestHandled(doctorId);
        verify(statisticsUpdateService).updateProgressAfterModifyRequest(doctorId);
        verify(badgeService).evaluateResponseRelatedBadges(doctorId);
    }

    @Test
    void evaluateAfterTurnCancellation_ValidDoctor_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        triggerService.evaluateAfterTurnCancellation(doctorId);

        verify(userRepository).findById(doctorId);
        verify(statisticsUpdateService).updateAfterTurnCancelled(doctorId);
        verify(statisticsUpdateService).updateProgressAfterCancellation(doctorId);
        verify(badgeService).evaluateConsistencyRelatedBadges(doctorId);
    }

    @Test
    void evaluateAllBadges_ValidDoctor_UpdatesAllProgressAndEvaluatesBadges() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        triggerService.evaluateAllBadges(doctorId);

        verify(userRepository).findById(doctorId);
        verify(statisticsUpdateService).updateAllBadgeProgress(doctorId);
        verify(badgeService).evaluateAllBadges(doctorId);
    }

    @Test
    void evaluateAllBadges_InvalidDoctorRole_HandlesGracefully() {
        User patient = new User();
        patient.setId(doctorId);
        patient.setRole("PATIENT");

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(patient));

        assertDoesNotThrow(() -> triggerService.evaluateAllBadges(doctorId));

        verify(userRepository).findById(doctorId);
        verifyNoInteractions(statisticsUpdateService);
        verifyNoInteractions(badgeService);
    }

    @Test
    void evaluateAllBadges_ExceptionInProgressUpdate_HandlesGracefully() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        doThrow(new RuntimeException("Progress update failed"))
            .when(statisticsUpdateService).updateAllBadgeProgress(doctorId);

        assertDoesNotThrow(() -> triggerService.evaluateAllBadges(doctorId));

        verify(userRepository).findById(doctorId);
        verify(statisticsUpdateService).updateAllBadgeProgress(doctorId);
        verify(badgeService, never()).evaluateAllBadges(any());
    }

    @Test
    void validateDoctorRole_DoctorRole_ValidatesSuccessfully() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        triggerService.evaluateAllBadges(doctorId);

        verify(userRepository).findById(doctorId);
    }

    @Test
    void validateDoctorRole_PatientRole_HandlesGracefully() {
        User patient = new User();
        patient.setId(doctorId);
        patient.setRole("PATIENT");

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(patient));

        assertDoesNotThrow(() -> triggerService.evaluateAllBadges(doctorId));

        verify(userRepository).findById(doctorId);
    }

    @Test
    void validateDoctorRole_UserNotFound_HandlesGracefully() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> triggerService.evaluateAllBadges(doctorId));

        verify(userRepository).findById(doctorId);
    }
}