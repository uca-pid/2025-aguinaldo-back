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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientBadgeEvaluationTriggerServiceTest {

    @Mock
    private PatientBadgeService badgeService;

    @Mock
    private PatientBadgeStatisticsUpdateService statisticsUpdateService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientBadgeEvaluationTriggerService triggerService;

    private UUID patientId;
    private UUID doctorId;
    private User patient;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        patient = new User();
        patient.setId(patientId);
        patient.setName("Test");
        patient.setSurname("Patient");
        patient.setRole("PATIENT");
        patient.setStatus("ACTIVE");
    }

    @Test
    void evaluateAfterTurnCompletion_ValidPatient_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAfterTurnCompletion(patientId, doctorId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterTurnCompleted(patientId, doctorId);
        verify(statisticsUpdateService).updateProgressAfterTurnCompletion(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAfterTurnCompletion_InvalidPatientRole_HandlesGracefully() {
        User doctor = new User();
        doctor.setId(patientId);
        doctor.setRole("DOCTOR");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(doctor));

        assertDoesNotThrow(() ->
            triggerService.evaluateAfterTurnCompletion(patientId, doctorId));

        verify(userRepository).findById(patientId);
        verifyNoInteractions(statisticsUpdateService);
        verifyNoInteractions(badgeService);
    }

    @Test
    void evaluateAfterTurnCompletion_PatientNotFound_HandlesGracefully() {
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
            triggerService.evaluateAfterTurnCompletion(patientId, doctorId));

        verify(userRepository).findById(patientId);
        verifyNoInteractions(statisticsUpdateService);
        verifyNoInteractions(badgeService);
    }

    @Test
    void evaluateAfterTurnCompletion_ExceptionInStatisticsUpdate_HandlesGracefully() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        doThrow(new RuntimeException("Statistics update failed"))
            .when(statisticsUpdateService).updateAfterTurnCompleted(any(), any());

        assertDoesNotThrow(() ->
            triggerService.evaluateAfterTurnCompletion(patientId, doctorId));

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterTurnCompleted(patientId, doctorId);
        verify(statisticsUpdateService, never()).updateProgressAfterTurnCompletion(any());
        verify(badgeService, never()).evaluateAllBadges(any());
    }

    @Test
    void evaluateAfterTurnCancellation_ValidPatient_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAfterTurnCancellation(patientId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterTurnCancelled(patientId);
        verify(statisticsUpdateService).updateProgressAfterTurnCompletion(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAfterTurnNoShow_ValidPatient_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAfterTurnNoShow(patientId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterTurnNoShow(patientId);
        verify(statisticsUpdateService).updateProgressAfterTurnCompletion(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAfterRatingGiven_ValidPatient_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAfterRatingGiven(patientId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterRatingGiven(patientId);
        verify(statisticsUpdateService).updateProgressAfterRating(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAfterRatingReceived_ValidPatient_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAfterRatingReceived(patientId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterRatingReceived(patientId);
        verify(statisticsUpdateService).updateProgressAfterRating(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAfterFileUploaded_ValidPatient_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAfterFileUploaded(patientId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterFileUploaded(patientId);
        verify(statisticsUpdateService).updateProgressAfterFileUpload(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAfterAdvanceBooking_ValidPatient_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAfterAdvanceBooking(patientId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterAdvanceBooking(patientId);
        verify(statisticsUpdateService).updateProgressAfterBooking(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAfterPunctualityRating_ValidPatient_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAfterPunctualityRating(patientId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterPunctualityRating(patientId);
        verify(statisticsUpdateService).updateProgressAfterRating(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAfterCollaborationRating_ValidPatient_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAfterCollaborationRating(patientId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterCollaborationRating(patientId);
        verify(statisticsUpdateService).updateProgressAfterRating(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAfterFollowInstructionsRating_ValidPatient_UpdatesStatisticsAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAfterFollowInstructionsRating(patientId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAfterFollowInstructionsRating(patientId);
        verify(statisticsUpdateService).updateProgressAfterRating(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAllBadges_ValidPatient_UpdatesAllProgressAndEvaluatesBadges() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAllBadges(patientId);

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAllBadgeProgress(patientId);
        verify(badgeService).evaluateAllBadges(patientId);
    }

    @Test
    void evaluateAllBadges_InvalidPatientRole_HandlesGracefully() {
        User doctor = new User();
        doctor.setId(patientId);
        doctor.setRole("DOCTOR");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(doctor));

        assertDoesNotThrow(() -> triggerService.evaluateAllBadges(patientId));

        verify(userRepository).findById(patientId);
        verifyNoInteractions(statisticsUpdateService);
        verifyNoInteractions(badgeService);
    }

    @Test
    void evaluateAllBadges_ExceptionInProgressUpdate_HandlesGracefully() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        doThrow(new RuntimeException("Progress update failed"))
            .when(statisticsUpdateService).updateAllBadgeProgress(patientId);

        assertDoesNotThrow(() -> triggerService.evaluateAllBadges(patientId));

        verify(userRepository).findById(patientId);
        verify(statisticsUpdateService).updateAllBadgeProgress(patientId);
        verify(badgeService, never()).evaluateAllBadges(any());
    }

    @Test
    void validatePatientRole_PatientRole_ValidatesSuccessfully() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        triggerService.evaluateAllBadges(patientId);

        verify(userRepository).findById(patientId);
    }

    @Test
    void validatePatientRole_DoctorRole_HandlesGracefully() {
        User doctor = new User();
        doctor.setId(patientId);
        doctor.setRole("DOCTOR");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(doctor));

        assertDoesNotThrow(() -> triggerService.evaluateAllBadges(patientId));

        verify(userRepository).findById(patientId);
    }

    @Test
    void validatePatientRole_UserNotFound_HandlesGracefully() {
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> triggerService.evaluateAllBadges(patientId));

        verify(userRepository).findById(patientId);
    }
}