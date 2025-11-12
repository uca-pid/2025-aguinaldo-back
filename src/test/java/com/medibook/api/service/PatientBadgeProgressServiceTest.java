package com.medibook.api.service;

import com.medibook.api.dto.Badge.PatientBadgeProgressSummaryDTO;
import com.medibook.api.entity.PatientBadgeStatistics;
import com.medibook.api.entity.PatientBadge;
import com.medibook.api.entity.PatientBadgeType;
import com.medibook.api.entity.PatientBadgeType.PatientBadgeCategory;
import com.medibook.api.entity.User;
import com.medibook.api.repository.PatientBadgeRepository;
import com.medibook.api.repository.PatientBadgeStatisticsRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientBadgeProgressServiceTest {

    @Mock
    private PatientBadgeStatisticsRepository statisticsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientBadgeRepository badgeRepository;

    @InjectMocks
    private PatientBadgeProgressService badgeProgressService;

    private UUID patientId;
    private User patient;
    private PatientBadgeStatistics stats;
    private List<PatientBadgeType> earnedBadges;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        patient = new User();
        patient.setId(patientId);
        patient.setName("Test");
        patient.setSurname("Patient");
        patient.setRole("PATIENT");
        patient.setStatus("ACTIVE");

        stats = PatientBadgeStatistics.builder()
                .patientId(patientId)
                .totalTurnsCompleted(50)
                .totalTurnsCancelled(5)
                .totalTurnsNoShow(2)
                .turnsLast12Months(25)
                .turnsLast6Months(15)
                .turnsLast90Days(8)
                .last5TurnsAttendanceRate(0.9)
                .last10TurnsPunctualCount(9)
                .last5TurnsAdvanceBookingCount(4)
                .last15TurnsCollaborationCount(12)
                .last15TurnsFollowInstructionsCount(11)
                .last10TurnsFilesUploadedCount(8)
                .totalRatingsGiven(20)
                .totalRatingsReceived(15)
                .avgRatingGiven(4.2)
                .avgRatingReceived(4.5)
                .totalUniqueDoctors(8)
                .turnsWithSameDoctorLast12Months(3)
                .differentSpecialtiesLast12Months(3)
                .progressMediBookWelcome(75.0)
                .progressHealthGuardian(60.0)
                .progressCommittedPatient(45.0)
                .progressContinuousFollowup(85.0)
                .progressConstantPatient(90.0)
                .progressExemplaryPunctuality(80.0)
                .progressSmartPlanner(78.0)
                .progressExcellentCollaborator(80.0)
                .progressAlwaysPrepared(65.0)
                .progressResponsibleEvaluator(0.0)
                .progressExcellenceModel(0.0)
                .build();

        earnedBadges = Arrays.asList(
            PatientBadgeType.MEDIBOOK_WELCOME,
            PatientBadgeType.CONSTANT_PATIENT,
            PatientBadgeType.EXCELLENT_COLLABORATOR
        );
    }

    @Test
    void getBadgeProgress_PatientFoundWithStatsAndBadges_ReturnsCompleteProgressList() {
        PatientBadgeStatistics highProgressStats = PatientBadgeStatistics.builder()
                .patientId(patientId)
                .totalTurnsCompleted(50)
                .totalTurnsCancelled(5)
                .totalTurnsNoShow(2)
                .turnsLast12Months(25)
                .turnsLast6Months(15)
                .turnsLast90Days(8)
                .last5TurnsAttendanceRate(0.9)
                .last10TurnsPunctualCount(9)
                .last5TurnsAdvanceBookingCount(4)
                .last15TurnsCollaborationCount(12)
                .last15TurnsFollowInstructionsCount(11)
                .last10TurnsFilesUploadedCount(8)
                .totalRatingsGiven(20)
                .totalRatingsReceived(15)
                .avgRatingGiven(4.2)
                .avgRatingReceived(4.5)
                .totalUniqueDoctors(8)
                .turnsWithSameDoctorLast12Months(3)
                .differentSpecialtiesLast12Months(3)
                .progressMediBookWelcome(85.0)  // Higher progress for earned badge
                .progressHealthGuardian(60.0)
                .progressCommittedPatient(45.0)
                .progressContinuousFollowup(85.0)
                .progressConstantPatient(95.0)   // Higher progress for earned badge
                .progressExemplaryPunctuality(80.0)
                .progressSmartPlanner(78.0)
                .progressExcellentCollaborator(90.0)  // Higher progress for earned badge
                .progressAlwaysPrepared(65.0)
                .progressResponsibleEvaluator(0.0)
                .progressExcellenceModel(0.0)
                .build();

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(highProgressStats));
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId)).thenReturn(earnedBadges.stream()
                .map(type -> {
                    PatientBadge badge = new PatientBadge();
                    badge.setBadgeType(type);
                    badge.setIsActive(true);
                    return badge;
                })
                .toList());

        List<PatientBadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(patientId);

        assertEquals(11, result.size());

        PatientBadgeProgressSummaryDTO preventiveBadge = result.get(0);
        assertEquals(PatientBadgeType.MEDIBOOK_WELCOME, preventiveBadge.getBadgeType());
        assertEquals("Bienvenido a MediBook", preventiveBadge.getBadgeName());
        assertEquals(PatientBadgeCategory.WELCOME, preventiveBadge.getCategory());
        assertEquals(85.0, preventiveBadge.getProgressPercentage());
        assertTrue(preventiveBadge.getEarned());
        assertEquals("Completa tu primer turno", preventiveBadge.getDescription());
        assertEquals("¡Insignia obtenida! Excelente trabajo.", preventiveBadge.getStatusMessage());

        PatientBadgeProgressSummaryDTO punctualBadge = result.get(4);
        assertEquals(PatientBadgeType.CONSTANT_PATIENT, punctualBadge.getBadgeType());
        assertEquals("Paciente Constante", punctualBadge.getBadgeName());
        assertTrue(punctualBadge.getEarned());

        PatientBadgeProgressSummaryDTO preparedBadge = result.get(7);
        assertEquals(PatientBadgeType.EXCELLENT_COLLABORATOR, preparedBadge.getBadgeType());
        assertEquals("Colaborador Excelente", preparedBadge.getBadgeName());
        assertTrue(preparedBadge.getEarned());

        PatientBadgeProgressSummaryDTO exemplaryBadge = result.get(10);
        assertEquals(PatientBadgeType.EXCELLENCE_MODEL, exemplaryBadge.getBadgeType());
        assertEquals("Modelo de Excelencia", exemplaryBadge.getBadgeName());
        assertEquals(0.0, exemplaryBadge.getProgressPercentage());
        assertFalse(exemplaryBadge.getEarned());
        assertEquals("Comienza a trabajar en esta insignia.", exemplaryBadge.getStatusMessage());

        verify(userRepository).findById(patientId);
        verify(statisticsRepository).findByPatientId(patientId);
        verify(badgeRepository).findByPatient_IdAndIsActiveTrue(patientId);
    }

    @Test
    void getBadgeProgress_PatientNotFound_ThrowsException() {
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> badgeProgressService.getBadgeProgress(patientId));

        assertEquals("Patient not found", exception.getMessage());
        verify(userRepository).findById(patientId);
        verifyNoInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void getBadgeProgress_UserNotPatient_ThrowsException() {
        User doctor = new User();
        doctor.setId(patientId);
        doctor.setRole("DOCTOR");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> badgeProgressService.getBadgeProgress(patientId));

        assertEquals("User is not a patient", exception.getMessage());
        verify(userRepository).findById(patientId);
        verifyNoInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void getBadgeProgress_NoStatsFound_UsesEmptyStats() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.empty());
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId)).thenReturn(Collections.emptyList());

        List<PatientBadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(patientId);

        assertEquals(11, result.size());

        result.forEach(badge -> {
            assertEquals(0.0, badge.getProgressPercentage());
            assertFalse(badge.getEarned());
            assertEquals("Comienza a trabajar en esta insignia.", badge.getStatusMessage());
        });

        verify(userRepository).findById(patientId);
        verify(statisticsRepository).findByPatientId(patientId);
        verify(badgeRepository).findByPatient_IdAndIsActiveTrue(patientId);
    }

    @Test
    void getBadgeProgress_NoEarnedBadges_AllBadgesNotEarned() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId)).thenReturn(Collections.emptyList());

        List<PatientBadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(patientId);

        assertEquals(11, result.size());

        result.forEach(badge -> {
            assertNotNull(badge.getProgressPercentage());
            assertFalse(badge.getEarned());
        });

        verify(userRepository).findById(patientId);
        verify(statisticsRepository).findByPatientId(patientId);
        verify(badgeRepository).findByPatient_IdAndIsActiveTrue(patientId);
    }

    @Test
    void getBadgeProgress_AllBadgesEarned_AllMarkedAsEarned() {
        List<PatientBadgeType> allEarnedBadges = Arrays.asList(PatientBadgeType.values());

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId)).thenReturn(allEarnedBadges.stream()
                .map(type -> {
                    PatientBadge badge = new PatientBadge();
                    badge.setBadgeType(type);
                    badge.setIsActive(true);
                    return badge;
                })
                .toList());

        List<PatientBadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(patientId);

        assertEquals(11, result.size());

        result.forEach(badge -> {
            assertTrue(badge.getEarned());
            assertEquals("¡Insignia obtenida! Excelente trabajo.", badge.getStatusMessage());
        });

        verify(userRepository).findById(patientId);
        verify(statisticsRepository).findByPatientId(patientId);
        verify(badgeRepository).findByPatient_IdAndIsActiveTrue(patientId);
    }

    @Test
    void getBadgeProgress_PartialProgress_StatusMessagesCorrect() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId)).thenReturn(Collections.emptyList());

        List<PatientBadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(patientId);

        PatientBadgeProgressSummaryDTO preventiveBadge = result.stream()
                .filter(b -> b.getBadgeType() == PatientBadgeType.MEDIBOOK_WELCOME)
                .findFirst().orElseThrow();
        PatientBadgeProgressSummaryDTO commitmentBadge = result.stream()
                .filter(b -> b.getBadgeType() == PatientBadgeType.HEALTH_GUARDIAN)
                .findFirst().orElseThrow();
        PatientBadgeProgressSummaryDTO punctualBadge = result.stream()
                .filter(b -> b.getBadgeType() == PatientBadgeType.CONSTANT_PATIENT)
                .findFirst().orElseThrow();

        assertEquals(75.0, preventiveBadge.getProgressPercentage());
        assertEquals("¡Casi lo logras! Sigue así.", preventiveBadge.getStatusMessage());

        assertEquals(60.0, commitmentBadge.getProgressPercentage());
        assertEquals("¡Buen progreso! Estás a mitad de camino.", commitmentBadge.getStatusMessage());

        assertEquals(90.0, punctualBadge.getProgressPercentage());
        assertEquals("¡Casi lo logras! Sigue así.", punctualBadge.getStatusMessage());

        verify(userRepository).findById(patientId);
        verify(statisticsRepository).findByPatientId(patientId);
        verify(badgeRepository).findByPatient_IdAndIsActiveTrue(patientId);
    }
}