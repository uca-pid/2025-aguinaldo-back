package com.medibook.api.service;

import com.medibook.api.dto.Badge.PatientBadgeDTO;
import com.medibook.api.dto.Badge.PatientBadgesResponseDTO;
import com.medibook.api.entity.*;
import com.medibook.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientBadgeServiceTest {

    @Mock
    private PatientBadgeRepository badgeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientBadgeStatisticsRepository statisticsRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private TurnAssignedRepository turnAssignedRepository;

    @InjectMocks
    private PatientBadgeService patientBadgeService;

    private UUID patientId;
    private User patient;
    private PatientBadgeStatistics stats;

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
                .patient(patient)
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
                .build();
    }

    @Test
    void getPatientBadges_PatientFound_ReturnsBadgesGroupedByCategory() {
        PatientBadge activeBadge1 = createBadge(PatientBadgeType.MEDIBOOK_WELCOME, true);
        PatientBadge activeBadge2 = createBadge(PatientBadgeType.CONSTANT_PATIENT, true);
        PatientBadge activeBadge3 = createBadge(PatientBadgeType.ALWAYS_PREPARED, true);

        List<PatientBadge> allBadges = Arrays.asList(activeBadge1, activeBadge2, activeBadge3);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(badgeRepository.findByPatient_IdOrderByEarnedAtDesc(any())).thenReturn(new ArrayList<>());
        when(badgeRepository.findByPatient_IdOrderByEarnedAtDesc(eq(patientId))).thenReturn(allBadges);

        PatientBadgesResponseDTO result = patientBadgeService.getPatientBadges(patientId);

        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        assertEquals("Test Patient", result.getPatientName());
        assertEquals(3, result.getTotalActiveBadges());

        long totalCategories = 0;
        if (!result.getWelcomeBadges().isEmpty()) totalCategories++;
        if (!result.getPreventiveCareBadges().isEmpty()) totalCategories++;
        if (!result.getActiveCommitmentBadges().isEmpty()) totalCategories++;
        if (!result.getClinicalExcellenceBadges().isEmpty()) totalCategories++;
        assertEquals(3, totalCategories);

        assertEquals(1, result.getWelcomeBadges().size());
        assertTrue(result.getWelcomeBadges().stream()
                .anyMatch(b -> b.getBadgeType() == PatientBadgeType.MEDIBOOK_WELCOME && b.getIsActive()));

        assertEquals(1, result.getPreventiveCareBadges().size());
        assertTrue(result.getPreventiveCareBadges().stream()
                .anyMatch(b -> b.getBadgeType() == PatientBadgeType.CONSTANT_PATIENT && b.getIsActive()));

        assertEquals(0, result.getActiveCommitmentBadges().size());

        assertEquals(1, result.getClinicalExcellenceBadges().size());
        assertTrue(result.getClinicalExcellenceBadges().stream()
                .anyMatch(b -> b.getBadgeType() == PatientBadgeType.ALWAYS_PREPARED && b.getIsActive()));

        verify(userRepository).findById(patientId);
        verify(badgeRepository).findByPatient_IdOrderByEarnedAtDesc(patientId);
    }

    @Test
    void getPatientBadges_PatientNotFound_ThrowsException() {
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> patientBadgeService.getPatientBadges(patientId));

        assertEquals("Patient not found", exception.getMessage());
        verify(userRepository).findById(patientId);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void getPatientBadges_UserNotPatient_ThrowsException() {
        User doctor = new User();
        doctor.setId(patientId);
        doctor.setRole("DOCTOR");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> patientBadgeService.getPatientBadges(patientId));

        assertEquals("User is not a patient", exception.getMessage());
        verify(userRepository).findById(patientId);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void evaluateAllBadges_PatientFound_EvaluatesAllBadgeTypes() {
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        patientBadgeService.evaluateAllBadges(patientId);

        verify(badgeRepository, atLeast(10)).findByPatient_IdAndBadgeType(any(), any());
        verify(userRepository).findById(patientId);
        verify(statisticsRepository, atLeastOnce()).findByPatientId(patientId);
    }

    @Test
    void evaluateAllBadges_PatientNotFound_ThrowsException() {
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> patientBadgeService.evaluateAllBadges(patientId));

        assertEquals("Patient not found", exception.getMessage());
        verify(userRepository).findById(patientId);
    }

    @Test
    void evaluateAllBadges_UserNotPatient_ThrowsException() {
        User doctor = new User();
        doctor.setId(patientId);
        doctor.setRole("DOCTOR");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> patientBadgeService.evaluateAllBadges(patientId));

        assertEquals("User is not a patient", exception.getMessage());
        verify(userRepository).findById(patientId);
    }

    @Test
    void evaluateMediBookWelcome_InsufficientTurns_DeactivatesBadge() throws Exception {
        PatientBadgeStatistics lowStats = PatientBadgeStatistics.builder()
                .patient(patient)
                .totalTurnsCompleted(0) // Less than required 1
                .build();

        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(lowStats));

        PatientBadge existingBadge = createBadge(PatientBadgeType.MEDIBOOK_WELCOME, true);
        when(badgeRepository.findByPatient_IdAndBadgeType(patientId, PatientBadgeType.MEDIBOOK_WELCOME))
            .thenReturn(Optional.of(existingBadge));

        Method method = PatientBadgeService.class.getDeclaredMethod("evaluateMediBookWelcome", User.class);
        method.setAccessible(true);
        method.invoke(patientBadgeService, patient);

        verify(badgeRepository).findByPatient_IdAndBadgeType(patientId, PatientBadgeType.MEDIBOOK_WELCOME);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }




    @Test
    void evaluateConstantPatient_InsufficientTurns_DeactivatesBadge() throws Exception {
        PatientBadgeStatistics lowStats = PatientBadgeStatistics.builder()
                .patient(patient)
                .totalTurnsCompleted(10) // Less than required 15
                .turnsLast6Months(0)
                .build();

        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(lowStats));

        PatientBadge existingBadge = createBadge(PatientBadgeType.CONSTANT_PATIENT, true);
        when(badgeRepository.findByPatient_IdAndBadgeType(patientId, PatientBadgeType.CONSTANT_PATIENT))
            .thenReturn(Optional.of(existingBadge));

        Method method = PatientBadgeService.class.getDeclaredMethod("evaluateConstantPatient", User.class);
        method.setAccessible(true);
        method.invoke(patientBadgeService, patient);

        verify(badgeRepository).findByPatient_IdAndBadgeType(patientId, PatientBadgeType.CONSTANT_PATIENT);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }



    @Test
    void evaluateAlwaysPrepared_InsufficientTurns_DeactivatesBadge() throws Exception {
        PatientBadgeStatistics lowStats = PatientBadgeStatistics.builder()
                .patient(patient)
                .totalTurnsCompleted(5) // Less than required 7
                .last10TurnsFilesUploadedCount(6)
                .build();

        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(lowStats));

        PatientBadge existingBadge = createBadge(PatientBadgeType.ALWAYS_PREPARED, true);
        when(badgeRepository.findByPatient_IdAndBadgeType(patientId, PatientBadgeType.ALWAYS_PREPARED))
            .thenReturn(Optional.of(existingBadge));

        Method method = PatientBadgeService.class.getDeclaredMethod("evaluateAlwaysPrepared", User.class);
        method.setAccessible(true);
        method.invoke(patientBadgeService, patient);

        verify(badgeRepository).findByPatient_IdAndBadgeType(patientId, PatientBadgeType.ALWAYS_PREPARED);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }


    @Test
    void evaluateExcellenceModel_InsufficientTurns_DeactivatesBadge() throws Exception {
        PatientBadgeStatistics lowStats = PatientBadgeStatistics.builder()
                .patient(patient)
                .totalTurnsCompleted(20) // Less than required 25
                .turnsLast90Days(5)
                .avgRatingReceived(4.2)
                .build();

        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(lowStats));

        PatientBadge existingBadge = createBadge(PatientBadgeType.EXCELLENCE_MODEL, true);
        when(badgeRepository.findByPatient_IdAndBadgeType(patientId, PatientBadgeType.EXCELLENCE_MODEL))
            .thenReturn(Optional.of(existingBadge));

        Method method = PatientBadgeService.class.getDeclaredMethod("evaluateExcellenceModel", User.class);
        method.setAccessible(true);
        method.invoke(patientBadgeService, patient);

        verify(badgeRepository).findByPatient_IdAndBadgeType(patientId, PatientBadgeType.EXCELLENCE_MODEL);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void getOrCreateStatistics_NoExistingStatistics_CreatesDefaultStatistics() throws Exception {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.empty());
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(statisticsRepository.save(any(PatientBadgeStatistics.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Method method = PatientBadgeService.class.getDeclaredMethod("getOrCreateStatistics", UUID.class);
        method.setAccessible(true);
        PatientBadgeStatistics result = (PatientBadgeStatistics) method.invoke(patientBadgeService, patientId);

        assertNotNull(result);
        assertEquals(patient, result.getPatient());
        assertEquals(0, result.getTotalTurnsCompleted());
        assertEquals(0, result.getTotalTurnsCancelled());
        assertEquals(0, result.getTotalTurnsNoShow());
        assertEquals(0, result.getTurnsLast12Months());
        assertEquals(0, result.getTurnsLast6Months());
        assertEquals(0, result.getTurnsLast90Days());
        assertEquals(0.0, result.getLast5TurnsAttendanceRate());
        assertEquals(0, result.getLast10TurnsPunctualCount());
        assertEquals(0, result.getLast5TurnsAdvanceBookingCount());
        assertEquals(0, result.getLast15TurnsCollaborationCount());
        assertEquals(0, result.getLast15TurnsFollowInstructionsCount());
        assertEquals(0, result.getLast10TurnsFilesUploadedCount());
        assertEquals(0, result.getTotalRatingsGiven());
        assertEquals(0, result.getTotalRatingsReceived());
        assertNull(result.getAvgRatingGiven());
        assertNull(result.getAvgRatingReceived());
        assertEquals(0, result.getTotalUniqueDoctors());
        assertEquals(0, result.getTurnsWithSameDoctorLast12Months());
        assertEquals(0, result.getDifferentSpecialtiesLast12Months());

        verify(statisticsRepository).save(any(PatientBadgeStatistics.class));
    }

    private TurnAssigned createMockTurn() {
        TurnAssigned turn = new TurnAssigned();
        turn.setId(UUID.randomUUID());
        turn.setScheduledAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(10));
        turn.setStatus("COMPLETED");

        User doctor = new User();
        doctor.setId(UUID.randomUUID());
        turn.setDoctor(doctor);

        return turn;
    }

    private PatientBadge createBadge(PatientBadgeType badgeType, boolean isActive) {
        PatientBadge badge = new PatientBadge();
        badge.setBadgeType(badgeType);
        badge.setIsActive(isActive);
        badge.setEarnedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(30));
        badge.setLastEvaluatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return badge;
    }
}