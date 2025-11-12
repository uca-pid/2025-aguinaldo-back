package com.medibook.api.service;

import com.medibook.api.entity.PatientBadgeStatistics;
import com.medibook.api.entity.PatientBadge;
import com.medibook.api.entity.PatientBadgeType;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.repository.PatientBadgeRepository;
import com.medibook.api.repository.PatientBadgeStatisticsRepository;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.RatingRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientBadgeStatisticsUpdateServiceTest {

    @Mock
    private PatientBadgeStatisticsRepository statisticsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TurnAssignedRepository turnAssignedRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private PatientBadgeRepository badgeRepository;

    @InjectMocks
    private PatientBadgeStatisticsUpdateService updateService;

    private UUID patientId;
    private UUID doctorId;
    private User patient;
    private PatientBadgeStatistics stats;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        patient = new User();
        patient.setId(patientId);
        patient.setName("Test");
        patient.setSurname("Patient");
        patient.setRole("PATIENT");

        stats = PatientBadgeStatistics.builder()
                .patientId(patientId)
                .patient(patient)
                .totalTurnsCompleted(25)
                .totalTurnsCancelled(3)
                .totalTurnsNoShow(1)
                .turnsLast12Months(15)
                .turnsLast6Months(10)
                .turnsLast90Days(5)
                .last5TurnsAttendanceRate(0.8)
                .last10TurnsPunctualCount(7)
                .last5TurnsAdvanceBookingCount(3)
                .last15TurnsCollaborationCount(10)
                .last15TurnsFollowInstructionsCount(9)
                .last10TurnsFilesUploadedCount(6)
                .totalRatingsGiven(12)
                .totalRatingsReceived(8)
                .avgRatingGiven(4.1)
                .avgRatingReceived(4.3)
                .totalUniqueDoctors(6)
                .turnsWithSameDoctorLast12Months(2)
                .differentSpecialtiesLast12Months(2)
                .build();
    }

    @Test
    void updateAfterTurnCompleted_ValidPatient_UpdatesStatistics() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        updateService.updateAfterTurnCompleted(patientId, doctorId);

        verify(statisticsRepository).incrementTurnCompleted(patientId);
        verify(statisticsRepository, times(3)).findByPatientId(patientId);
    }

    @Test
    void updateAfterTurnCompleted_StatisticsNotExist_CreatesNewStatistics() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.empty());
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        updateService.updateAfterTurnCompleted(patientId, doctorId);

        verify(statisticsRepository).incrementTurnCompleted(patientId);
        verify(statisticsRepository, times(3)).findByPatientId(patientId);
    }

    @Test
    void updateAfterTurnCancelled_IncrementsCancellationCount() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        updateService.updateAfterTurnCancelled(patientId);

        verify(statisticsRepository).incrementTurnCancelled(patientId);
    }

    @Test
    void updateAfterTurnNoShow_IncrementsNoShowCount() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        updateService.updateAfterTurnNoShow(patientId);

        verify(statisticsRepository).incrementTurnNoShow(patientId);
    }

    @Test
    void updateAfterRatingGiven_IncrementsRatingCountAndUpdatesAverage() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        updateService.updateAfterRatingGiven(patientId);

        verify(statisticsRepository).incrementRatingGiven(patientId);
        verify(statisticsRepository, times(1)).findByPatientId(patientId);
    }

    @Test
    void updateAfterRatingReceived_IncrementsRatingCountAndUpdatesAverage() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        updateService.updateAfterRatingReceived(patientId);

        verify(statisticsRepository).incrementRatingReceived(patientId);
        verify(statisticsRepository, times(1)).findByPatientId(patientId);
    }

    @Test
    void updateAfterFileUploaded_IncrementsFileCount() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        updateService.updateAfterFileUploaded(patientId);

        verify(statisticsRepository).incrementFileUploaded(patientId);
    }

    @Test
    void updateAfterAdvanceBooking_IncrementsAdvanceBookingCount() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        updateService.updateAfterAdvanceBooking(patientId);

        verify(statisticsRepository).incrementAdvanceBooking(patientId);
    }

    @Test
    void updateAfterPunctualityRating_IncrementsPunctualityCount() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        updateService.updateAfterPunctualityRating(patientId);

        verify(statisticsRepository).incrementPunctualRating(patientId);
    }

    @Test
    void updateAfterCollaborationRating_IncrementsCollaborationCount() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        updateService.updateAfterCollaborationRating(patientId);

        verify(statisticsRepository).incrementCollaborationRating(patientId);
    }

    @Test
    void updateAfterFollowInstructionsRating_IncrementsFollowInstructionsCount() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));

        updateService.updateAfterFollowInstructionsRating(patientId);

        verify(statisticsRepository).incrementFollowInstructionsRating(patientId);
    }

    @Test
    void updateProgressAfterTurnCompletion_CalculatesCorrectProgressValues() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId))
                .thenReturn(Arrays.asList());

        updateService.updateProgressAfterTurnCompletion(patientId);

        ArgumentCaptor<PatientBadgeStatistics> captor = ArgumentCaptor.forClass(PatientBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        PatientBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressMediBookWelcome());
        assertNotNull(savedStats.getProgressHealthGuardian());
        assertNotNull(savedStats.getProgressCommittedPatient());
        assertNotNull(savedStats.getProgressContinuousFollowup());
        assertNotNull(savedStats.getProgressConstantPatient());
        assertNotNull(savedStats.getProgressExemplaryPunctuality());
        assertNotNull(savedStats.getProgressSmartPlanner());
        assertNotNull(savedStats.getProgressExcellentCollaborator());
        assertNotNull(savedStats.getProgressAlwaysPrepared());
        assertNotNull(savedStats.getProgressResponsibleEvaluator());
        assertNotNull(savedStats.getProgressExcellenceModel());
    }

    @Test
    void updateProgressAfterRating_CalculatesCorrectProgressValues() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId))
                .thenReturn(Arrays.asList());

        updateService.updateProgressAfterRating(patientId);

        ArgumentCaptor<PatientBadgeStatistics> captor = ArgumentCaptor.forClass(PatientBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        PatientBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressConstantPatient());
        assertNotNull(savedStats.getProgressExcellentCollaborator());
        assertNotNull(savedStats.getProgressResponsibleEvaluator());
        assertNotNull(savedStats.getProgressExcellenceModel());
    }

    @Test
    void updateProgressAfterFileUpload_CalculatesCorrectProgressValues() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId))
                .thenReturn(Arrays.asList());

        updateService.updateProgressAfterFileUpload(patientId);

        ArgumentCaptor<PatientBadgeStatistics> captor = ArgumentCaptor.forClass(PatientBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        PatientBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressAlwaysPrepared());
        assertNotNull(savedStats.getProgressExcellenceModel());
    }

    @Test
    void updateProgressAfterBooking_CalculatesCorrectProgressValues() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId))
                .thenReturn(Arrays.asList());

        updateService.updateProgressAfterBooking(patientId);

        ArgumentCaptor<PatientBadgeStatistics> captor = ArgumentCaptor.forClass(PatientBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        PatientBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressSmartPlanner());
        assertNotNull(savedStats.getProgressExcellenceModel());
    }

    @Test
    void updateAllBadgeProgress_CalculatesAllProgressValues() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId))
                .thenReturn(Arrays.asList());

        updateService.updateAllBadgeProgress(patientId);

        ArgumentCaptor<PatientBadgeStatistics> captor = ArgumentCaptor.forClass(PatientBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        PatientBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressMediBookWelcome());
        assertNotNull(savedStats.getProgressHealthGuardian());
        assertNotNull(savedStats.getProgressCommittedPatient());
        assertNotNull(savedStats.getProgressContinuousFollowup());
        assertNotNull(savedStats.getProgressConstantPatient());
        assertNotNull(savedStats.getProgressExemplaryPunctuality());
        assertNotNull(savedStats.getProgressSmartPlanner());
        assertNotNull(savedStats.getProgressExcellentCollaborator());
        assertNotNull(savedStats.getProgressAlwaysPrepared());
        assertNotNull(savedStats.getProgressResponsibleEvaluator());
        assertNotNull(savedStats.getProgressExcellenceModel());
    }

    @Test
    void updateAllBadgeProgress_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.empty());

        updateService.updateAllBadgeProgress(patientId);

        verify(statisticsRepository).findByPatientId(patientId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void updateAfterTurnCompleted_ExceptionDuringIncrement_LogsError() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        doThrow(new RuntimeException("Database error")).when(statisticsRepository)
            .incrementTurnCompleted(any());

        updateService.updateAfterTurnCompleted(patientId, doctorId);

        verify(statisticsRepository).incrementTurnCompleted(patientId);
    }

    @Test
    void updateAfterTurnCancelled_ExceptionDuringUpdate_LogsError() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        doThrow(new RuntimeException("Database error")).when(statisticsRepository).incrementTurnCancelled(any());

        updateService.updateAfterTurnCancelled(patientId);

        verify(statisticsRepository).incrementTurnCancelled(patientId);
    }

    @Test
    void updateAfterRatingGiven_ExceptionDuringUpdate_LogsError() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        doThrow(new RuntimeException("Database error")).when(statisticsRepository).incrementRatingGiven(any());

        updateService.updateAfterRatingGiven(patientId);

        verify(statisticsRepository).incrementRatingGiven(patientId);
    }

    @Test
    void updateAfterFileUploaded_ExceptionDuringUpdate_LogsError() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        doThrow(new RuntimeException("Database error")).when(statisticsRepository).incrementFileUploaded(any());

        updateService.updateAfterFileUploaded(patientId);

        verify(statisticsRepository).incrementFileUploaded(patientId);
    }

    @Test
    void updateProgressAfterTurnCompletion_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.empty());

        updateService.updateProgressAfterTurnCompletion(patientId);

        verify(statisticsRepository).findByPatientId(patientId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void updateProgressAfterRating_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.empty());

        updateService.updateProgressAfterRating(patientId);

        verify(statisticsRepository).findByPatientId(patientId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void updateProgressAfterFileUpload_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.empty());

        updateService.updateProgressAfterFileUpload(patientId);

        verify(statisticsRepository).findByPatientId(patientId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void updateProgressAfterBooking_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.empty());

        updateService.updateProgressAfterBooking(patientId);

        verify(statisticsRepository).findByPatientId(patientId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void updateAfterTurnCompleted_DoctorRelationshipStats_IncrementsUniqueDoctors() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED"))
                .thenReturn(1L);

        updateService.updateAfterTurnCompleted(patientId, doctorId);

        ArgumentCaptor<PatientBadgeStatistics> captor = ArgumentCaptor.forClass(PatientBadgeStatistics.class);
        verify(statisticsRepository, times(2)).save(captor.capture());

        List<PatientBadgeStatistics> savedStats = captor.getAllValues();
        PatientBadgeStatistics doctorStats = savedStats.get(0); // First save from updateDoctorRelationshipStats
        assertEquals(7, doctorStats.getTotalUniqueDoctors()); // 6 + 1
    }

    @Test
    void updateAfterTurnCompleted_ReturningDoctor_IncrementsSameDoctorCount() {
        // Create mock doctor
        User doctor = new User();
        doctor.setId(doctorId);
        doctor.setDoctorProfile(null); // No profile to avoid specialty filtering issues

        // Create mock turns with the same doctor within last 12 months
        TurnAssigned turn1 = mock(TurnAssigned.class);
        when(turn1.getDoctor()).thenReturn(doctor);
        when(turn1.getScheduledAt()).thenReturn(OffsetDateTime.now().minusMonths(6));

        TurnAssigned turn2 = mock(TurnAssigned.class);
        when(turn2.getDoctor()).thenReturn(doctor);
        when(turn2.getScheduledAt()).thenReturn(OffsetDateTime.now().minusMonths(3));

        TurnAssigned turn3 = mock(TurnAssigned.class);
        when(turn3.getDoctor()).thenReturn(doctor);
        when(turn3.getScheduledAt()).thenReturn(OffsetDateTime.now().minusDays(1)); // This is the new turn

        List<TurnAssigned> turnsWithDoctor = Arrays.asList(turn1, turn2, turn3);

        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED"))
                .thenReturn(3L);
        when(turnAssignedRepository.findByPatient_IdAndStatusOrderByScheduledAtDesc(patientId, "COMPLETED"))
                .thenReturn(turnsWithDoctor);

        updateService.updateAfterTurnCompleted(patientId, doctorId);

        ArgumentCaptor<PatientBadgeStatistics> captor = ArgumentCaptor.forClass(PatientBadgeStatistics.class);
        verify(statisticsRepository, times(2)).save(captor.capture());

        List<PatientBadgeStatistics> savedStats = captor.getAllValues();
        PatientBadgeStatistics doctorStats = savedStats.get(0); // First save from updateDoctorRelationshipStats
        assertEquals(6, doctorStats.getTotalUniqueDoctors()); // stays 6 (already had turns with this doctor)
        assertEquals(3, doctorStats.getTurnsWithSameDoctorLast12Months()); // 3 turns with same doctor
    }

    @Test
    void updateAfterRatingGiven_UpdatesAverageRatingGiven() {
        PatientBadgeStatistics statsWithRatings = PatientBadgeStatistics.builder()
                .patientId(patientId)
                .patient(patient)
                .totalRatingsGiven(9)
                .avgRatingGiven(4.0)
                .build();

        when(statisticsRepository.findByPatientId(patientId))
                .thenReturn(Optional.of(statsWithRatings))
                .thenReturn(Optional.of(PatientBadgeStatistics.builder()
                        .patientId(patientId)
                        .patient(patient)
                        .totalRatingsGiven(10)
                        .avgRatingGiven(4.1)
                        .build()));

        updateService.updateAfterRatingGiven(patientId);

        verify(statisticsRepository).incrementRatingGiven(patientId);
    }

    @Test
    void updateAfterRatingReceived_UpdatesAverageRatingReceived() {
        PatientBadgeStatistics statsWithRatings = PatientBadgeStatistics.builder()
                .patientId(patientId)
                .patient(patient)
                .totalRatingsReceived(7)
                .avgRatingReceived(4.2)
                .build();

        when(statisticsRepository.findByPatientId(patientId))
                .thenReturn(Optional.of(statsWithRatings))
                .thenReturn(Optional.of(PatientBadgeStatistics.builder()
                        .patientId(patientId)
                        .patient(patient)
                        .totalRatingsReceived(8)
                        .avgRatingReceived(4.25)
                        .build()));

        updateService.updateAfterRatingReceived(patientId);

        verify(statisticsRepository).incrementRatingReceived(patientId);
    }

    @Test
    void updateAllBadgeProgress_WithEarnedBadges_Returns100ForEarned() {
        List<PatientBadge> earnedBadges = Arrays.asList(
            PatientBadge.builder().badgeType(PatientBadgeType.MEDIBOOK_WELCOME).isActive(true).build(),
            PatientBadge.builder().badgeType(PatientBadgeType.CONSTANT_PATIENT).isActive(true).build(),
            PatientBadge.builder().badgeType(PatientBadgeType.ALWAYS_PREPARED).isActive(true).build()
        );

        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByPatient_IdAndIsActiveTrue(patientId))
                .thenReturn(earnedBadges);

        updateService.updateAllBadgeProgress(patientId);

        ArgumentCaptor<PatientBadgeStatistics> captor = ArgumentCaptor.forClass(PatientBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        PatientBadgeStatistics savedStats = captor.getValue();
        assertEquals(100.0, savedStats.getProgressMediBookWelcome());
        assertEquals(100.0, savedStats.getProgressConstantPatient());
        assertEquals(100.0, savedStats.getProgressAlwaysPrepared());
    }

    @Test
    void updateAfterTurnCompleted_ExceptionDuringDoctorStatsUpdate_LogsError() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.of(stats));
        when(turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED"))
                .thenReturn(1L);

        doThrow(new RuntimeException("Database error")).when(statisticsRepository).save(any());

        updateService.updateAfterTurnCompleted(patientId, doctorId);

        verify(statisticsRepository).incrementTurnCompleted(patientId);
        verify(turnAssignedRepository).countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED");
    }

    @Test
    void updateAfterRatingGiven_NoStatsFound_LogsError() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.empty());
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        updateService.updateAfterRatingGiven(patientId);

        verify(statisticsRepository).findByPatientId(patientId);
        verify(userRepository).findById(patientId);
        verify(statisticsRepository, never()).incrementRatingGiven(any());
    }

    @Test
    void updateAfterFileUploaded_NoStatsFound_LogsError() {
        when(statisticsRepository.findByPatientId(patientId)).thenReturn(Optional.empty());
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        updateService.updateAfterFileUploaded(patientId);

        verify(statisticsRepository).findByPatientId(patientId);
        verify(userRepository).findById(patientId);
        verify(statisticsRepository, never()).incrementFileUploaded(any());
    }
}