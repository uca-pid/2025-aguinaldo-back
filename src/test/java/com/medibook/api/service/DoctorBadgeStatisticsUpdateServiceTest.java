package com.medibook.api.service;

import com.medibook.api.entity.BadgeType;
import com.medibook.api.entity.DoctorBadge;
import com.medibook.api.entity.DoctorBadgeStatistics;
import com.medibook.api.entity.User;
import com.medibook.api.repository.DoctorBadgeRepository;
import com.medibook.api.repository.DoctorBadgeStatisticsRepository;
import com.medibook.api.repository.RatingRepository;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorBadgeStatisticsUpdateServiceTest {

    @Mock
    private DoctorBadgeStatisticsRepository statisticsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TurnAssignedRepository turnAssignedRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private DoctorBadgeRepository badgeRepository;

    @InjectMocks
    private DoctorBadgeStatisticsUpdateService updateService;

    private UUID doctorId;
    private UUID patientId;
    private User doctor;
    private DoctorBadgeStatistics stats;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctor = new User();
        doctor.setId(doctorId);
        doctor.setName("Dr. Test");
        doctor.setSurname("Doctor");
        doctor.setRole("DOCTOR");

        stats = DoctorBadgeStatistics.builder()
                .doctorId(doctorId)
                .doctor(doctor)
                .totalRatingsReceived(50)
                .last50CommunicationCount(10)
                .last50EmpathyCount(12)
                .last50PunctualityCount(8)
                .totalTurnsCompleted(100)
                .totalTurnsCancelled(5)
                .turnsLast90Days(20)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(45)
                .last30DocumentedCount(25)
                .last30TotalWords(4000)
                .last30AvgWordsPerEntry(160.0)
                .totalUniquePatients(50)
                .returningPatientsCount(8)
                .last10RequestsHandled(5)
                .build();
    }

    @Test
    void updateAfterRatingAdded_ValidScores_UpdatesCommunicationEmpathyPunctuality() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        updateService.updateAfterRatingAdded(doctorId, 4, 5, 3);

        verify(statisticsRepository).incrementRatingCounters(doctorId, true, true, false);
        verify(statisticsRepository, times(2)).findByDoctorId(doctorId);
    }

    @Test
    void updateAfterRatingAdded_NullScores_DoesNotUpdateCounters() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        updateService.updateAfterRatingAdded(doctorId, null, null, null);

        verify(statisticsRepository).incrementRatingCounters(doctorId, false, false, false);
        verify(statisticsRepository, times(2)).findByDoctorId(doctorId);
    }

    @Test
    void updateAfterRatingAdded_LowScores_DoesNotIncrementCounters() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        updateService.updateAfterRatingAdded(doctorId, 2, 3, 1);

        verify(statisticsRepository).incrementRatingCounters(doctorId, false, false, false);
        verify(statisticsRepository, times(2)).findByDoctorId(doctorId);
    }

    @Test
    void updateAfterRatingAdded_StatisticsNotExist_CreatesNewStatistics() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.empty());
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        updateService.updateAfterRatingAdded(doctorId, 4, 5, 4);

        verify(statisticsRepository).incrementRatingCounters(doctorId, true, true, true);
        verify(statisticsRepository, times(2)).findByDoctorId(doctorId);
    }

    @Test
    void updateAfterRatingAdded_ReachesRecalculationThreshold_TriggersRecalculation() {
        stats.setTotalRatingsReceived(49);
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        DoctorBadgeStatistics statsAfterIncrement = DoctorBadgeStatistics.builder()
                .doctorId(doctorId)
                .doctor(doctor)
                .totalRatingsReceived(50)
                .last50CommunicationCount(10)
                .last50EmpathyCount(12)
                .last50PunctualityCount(8)
                .totalTurnsCompleted(100)
                .totalTurnsCancelled(5)
                .turnsLast90Days(20)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(45)
                .last30DocumentedCount(25)
                .last30TotalWords(4000)
                .last30AvgWordsPerEntry(160.0)
                .totalUniquePatients(50)
                .returningPatientsCount(8)
                .last10RequestsHandled(5)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId))
                .thenReturn(Optional.of(stats))
                .thenReturn(Optional.of(statsAfterIncrement));

        updateService.updateAfterRatingAdded(doctorId, 4, 5, 4);

        verify(statisticsRepository).incrementRatingCounters(doctorId, true, true, true);
    }

    @Test
    void updateAfterTurnCompleted_IncrementsTurnCountAndUpdatesPatientStats() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED"))
                .thenReturn(1L);

        updateService.updateAfterTurnCompleted(doctorId, patientId);

        verify(statisticsRepository).incrementTurnCompleted(doctorId);
        verify(turnAssignedRepository).countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED");

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(51, savedStats.getTotalUniquePatients());
        assertEquals(8, savedStats.getReturningPatientsCount());
    }

    @Test
    void updateAfterTurnCompleted_ReturningPatient_IncrementsReturningCount() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED"))
                .thenReturn(2L);

        updateService.updateAfterTurnCompleted(doctorId, patientId);

        verify(statisticsRepository).incrementTurnCompleted(doctorId);
        verify(turnAssignedRepository).countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED");

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(50, savedStats.getTotalUniquePatients());
        assertEquals(9, savedStats.getReturningPatientsCount());
    }

    @Test
    void updateAfterTurnCancelled_IncrementsCancellationCount() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        updateService.updateAfterTurnCancelled(doctorId);

        verify(statisticsRepository).incrementTurnCancelled(doctorId);
    }

    @Test
    void updateAfterMedicalHistoryDocumented_UpdatesDocumentationStats() {
        String content = "Patient shows symptoms of diabetes and hypertension";
        DoctorBadgeStatistics statsAfterIncrement = DoctorBadgeStatistics.builder()
                .doctorId(doctorId)
                .doctor(doctor)
                .totalRatingsReceived(50)
                .last50CommunicationCount(10)
                .last50EmpathyCount(12)
                .last50PunctualityCount(8)
                .totalTurnsCompleted(100)
                .totalTurnsCancelled(5)
                .turnsLast90Days(20)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(46)
                .last30DocumentedCount(26)
                .last30TotalWords(4000)
                .last30AvgWordsPerEntry(160.0)
                .totalUniquePatients(50)
                .returningPatientsCount(8)
                .last10RequestsHandled(5)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId))
                .thenReturn(Optional.of(stats))
                .thenReturn(Optional.of(statsAfterIncrement));

        updateService.updateAfterMedicalHistoryDocumented(doctorId, content);

        verify(statisticsRepository).incrementDocumentation(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(4007, savedStats.getLast30TotalWords());
        assertEquals(154.1153846153846, savedStats.getLast30AvgWordsPerEntry());
    }

    @Test
    void updateAfterMedicalHistoryDocumented_NullContent_HandlesGracefully() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        updateService.updateAfterMedicalHistoryDocumented(doctorId, null);

        verify(statisticsRepository).incrementDocumentation(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(4000, savedStats.getLast30TotalWords());
    }

    @Test
    void updateAfterModifyRequestHandled_IncrementsRequestCount() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        updateService.updateAfterModifyRequestHandled(doctorId);

        verify(statisticsRepository).incrementRequestHandled(doctorId);
    }

    @Test
    void updateProgressAfterRating_CalculatesCorrectProgressValues() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId))
                .thenReturn(Arrays.asList());

        updateService.updateProgressAfterRating(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressExcellenceInCare());
        assertNotNull(savedStats.getProgressEmpathyChampion());
        assertNotNull(savedStats.getProgressClearCommunicator());
        assertNotNull(savedStats.getProgressTimelyProfessional());
    }

    @Test
    void updateProgressAfterTurnCompletion_CalculatesCorrectProgressValues() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId))
                .thenReturn(Arrays.asList());

        updateService.updateProgressAfterTurnCompletion(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressRelationshipBuilder());
        assertNotNull(savedStats.getProgressTopSpecialist());
        assertNotNull(savedStats.getProgressMedicalLegend());
    }

    @Test
    void updateProgressAfterMedicalHistory_CalculatesCorrectProgressValues() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId))
                .thenReturn(Arrays.asList());

        updateService.updateProgressAfterMedicalHistory(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressReliableExpert());
        assertNotNull(savedStats.getProgressDetailedDiagnostician());
        assertNotNull(savedStats.getProgressMedicalLegend());
    }

    @Test
    void updateProgressAfterModifyRequest_CalculatesCorrectProgressValues() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId))
                .thenReturn(Arrays.asList());

        updateService.updateProgressAfterModifyRequest(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressAgileResponder());
    }

    @Test
    void updateProgressAfterCancellation_CalculatesCorrectProgressValues() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId))
                .thenReturn(Arrays.asList());

        updateService.updateProgressAfterCancellation(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressTimelyProfessional());
        assertNotNull(savedStats.getProgressFlexibleCaregiver());
        assertNotNull(savedStats.getProgressMedicalLegend());
    }

    @Test
    void updateAllBadgeProgress_CalculatesAllProgressValues() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId))
                .thenReturn(Arrays.asList());

        updateService.updateAllBadgeProgress(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertNotNull(savedStats.getProgressExcellenceInCare());
        assertNotNull(savedStats.getProgressEmpathyChampion());
        assertNotNull(savedStats.getProgressClearCommunicator());
        assertNotNull(savedStats.getProgressDetailedDiagnostician());
        assertNotNull(savedStats.getProgressTimelyProfessional());
        assertNotNull(savedStats.getProgressReliableExpert());
        assertNotNull(savedStats.getProgressFlexibleCaregiver());
        assertNotNull(savedStats.getProgressAgileResponder());
        assertNotNull(savedStats.getProgressRelationshipBuilder());
        assertNotNull(savedStats.getProgressTopSpecialist());
        assertNotNull(savedStats.getProgressMedicalLegend());
        assertNotNull(savedStats.getProgressAllStarDoctor());
    }

    @Test
    void updateAllBadgeProgress_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.empty());

        updateService.updateAllBadgeProgress(doctorId);

        verify(statisticsRepository).findByDoctorId(doctorId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void updateAfterRatingAdded_DoctorNotFound_LogsError() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.empty());
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        updateService.updateAfterRatingAdded(doctorId, 4, 5, 4);

        verify(statisticsRepository).findByDoctorId(doctorId);
        verify(userRepository).findById(doctorId);
        verify(statisticsRepository, never()).incrementRatingCounters(any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void updateAfterRatingAdded_ExceptionDuringIncrement_LogsError() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        doThrow(new RuntimeException("Database error")).when(statisticsRepository)
            .incrementRatingCounters(any(), anyBoolean(), anyBoolean(), anyBoolean());

        updateService.updateAfterRatingAdded(doctorId, 4, 5, 4);

        verify(statisticsRepository).incrementRatingCounters(doctorId, true, true, true);
    }

    @Test
    void updateAfterTurnCompleted_ExceptionDuringUpdate_LogsError() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        doThrow(new RuntimeException("Database error")).when(statisticsRepository).incrementTurnCompleted(doctorId);

        updateService.updateAfterTurnCompleted(doctorId, patientId);

        verify(statisticsRepository).incrementTurnCompleted(doctorId);
    }

    @Test
    void updateAfterTurnCancelled_ExceptionDuringUpdate_LogsError() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        doThrow(new RuntimeException("Database error")).when(statisticsRepository).incrementTurnCancelled(doctorId);

        updateService.updateAfterTurnCancelled(doctorId);

        verify(statisticsRepository).incrementTurnCancelled(doctorId);
    }

    @Test
    void updateAfterMedicalHistoryDocumented_ExceptionDuringUpdate_LogsError() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        doThrow(new RuntimeException("Database error")).when(statisticsRepository).incrementDocumentation(doctorId);

        updateService.updateAfterMedicalHistoryDocumented(doctorId, "Test content");

        verify(statisticsRepository).incrementDocumentation(doctorId);
    }

    @Test
    void updateAfterModifyRequestHandled_ExceptionDuringUpdate_LogsError() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        doThrow(new RuntimeException("Database error")).when(statisticsRepository).incrementRequestHandled(doctorId);

        updateService.updateAfterModifyRequestHandled(doctorId);

        verify(statisticsRepository).incrementRequestHandled(doctorId);
    }

    @Test
    void recalculateLast50Ratings_EmptyRatingsList_DoesNothing() {
        when(ratingRepository.findTop50ByRatedIdOrderByCreatedAtDesc(doctorId))
            .thenReturn(Arrays.asList());

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("recalculateLast50Ratings", UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        verify(ratingRepository).findTop50ByRatedIdOrderByCreatedAtDesc(doctorId);
        verify(statisticsRepository, never()).save(any());
    }

    @Test
    void recalculateLast50Ratings_WithNullSubcategory_SkipsRating() {
        com.medibook.api.entity.Rating rating = mock(com.medibook.api.entity.Rating.class);
        when(rating.getScore()).thenReturn(5);
        when(rating.getSubcategory()).thenReturn(null);

        when(ratingRepository.findTop50ByRatedIdOrderByCreatedAtDesc(doctorId))
            .thenReturn(Arrays.asList(rating));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("recalculateLast50Ratings", UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(0, savedStats.getLast50CommunicationCount());
        assertEquals(0, savedStats.getLast50EmpathyCount());
        assertEquals(0, savedStats.getLast50PunctualityCount());
    }

    @Test
    void recalculateLast50Ratings_WithLowScore_SkipsRating() {
        com.medibook.api.entity.Rating rating = mock(com.medibook.api.entity.Rating.class);
        when(rating.getScore()).thenReturn(3);

        when(ratingRepository.findTop50ByRatedIdOrderByCreatedAtDesc(doctorId))
            .thenReturn(Arrays.asList(rating));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("recalculateLast50Ratings", UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(0, savedStats.getLast50CommunicationCount());
        assertEquals(0, savedStats.getLast50EmpathyCount());
        assertEquals(0, savedStats.getLast50PunctualityCount());
    }

    @Test
    void containsCommunicationKeywords_VariousPhrases_ReturnsCorrectResults() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("containsCommunicationKeywords", String.class);
            method.setAccessible(true);

            assertTrue((Boolean) method.invoke(updateService, "explica claramente"));
            assertTrue((Boolean) method.invoke(updateService, "escucha atentamente"));
            assertTrue((Boolean) method.invoke(updateService, "comunica bien"));
            assertFalse((Boolean) method.invoke(updateService, "llega tarde"));
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void containsEmpathyKeywords_VariousPhrases_ReturnsCorrectResults() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("containsEmpathyKeywords", String.class);
            method.setAccessible(true);

            assertTrue((Boolean) method.invoke(updateService, "muestra empatía"));
            assertTrue((Boolean) method.invoke(updateService, "genera confianza"));
            assertTrue((Boolean) method.invoke(updateService, "es amable"));
            assertFalse((Boolean) method.invoke(updateService, "llega tarde"));
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void containsPunctualityKeywords_VariousPhrases_ReturnsCorrectResults() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("containsPunctualityKeywords", String.class);
            method.setAccessible(true);

            assertTrue((Boolean) method.invoke(updateService, "respeta horarios"));
            assertTrue((Boolean) method.invoke(updateService, "es puntual"));
            assertTrue((Boolean) method.invoke(updateService, "tiempo de espera"));
            assertFalse((Boolean) method.invoke(updateService, "no explica bien"));
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void updateProgressAfterRating_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.empty());

        updateService.updateProgressAfterRating(doctorId);

        verify(statisticsRepository).findByDoctorId(doctorId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void updateProgressAfterTurnCompletion_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.empty());

        updateService.updateProgressAfterTurnCompletion(doctorId);

        verify(statisticsRepository).findByDoctorId(doctorId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void updateProgressAfterMedicalHistory_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.empty());

        updateService.updateProgressAfterMedicalHistory(doctorId);

        verify(statisticsRepository).findByDoctorId(doctorId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void updateProgressAfterModifyRequest_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.empty());

        updateService.updateProgressAfterModifyRequest(doctorId);

        verify(statisticsRepository).findByDoctorId(doctorId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void updateProgressAfterCancellation_NoStatsFound_DoesNothing() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.empty());

        updateService.updateProgressAfterCancellation(doctorId);

        verify(statisticsRepository).findByDoctorId(doctorId);
        verifyNoMoreInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void calculateExcellenceProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateExcellenceProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.SUSTAINED_EXCELLENCE));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateExcellenceProgress_ZeroRatings_ReturnsZero() {
        DoctorBadgeStatistics zeroStats = DoctorBadgeStatistics.builder()
                .totalRatingsReceived(0)
                .last100AvgRating(0.0)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateExcellenceProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, zeroStats, Arrays.asList());

            assertEquals(0.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateEmpathyProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateEmpathyProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.EMPATHETIC_DOCTOR));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateEmpathyProgress_MaximumValues_Returns100() {
        DoctorBadgeStatistics maxStats = DoctorBadgeStatistics.builder()
                .last50EmpathyCount(20)
                .totalRatingsReceived(50)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateEmpathyProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, maxStats, Arrays.asList());

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateCommunicatorProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateCommunicatorProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.EXCEPTIONAL_COMMUNICATOR));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateDetailedDiagnostProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateDetailedDiagnostProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.DETAILED_HISTORIAN));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateDetailedDiagnostProgress_ZeroWords_ReturnsZero() {
        DoctorBadgeStatistics zeroStats = DoctorBadgeStatistics.builder()
                .last30AvgWordsPerEntry(0.0)
                .last30DocumentedCount(0)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateDetailedDiagnostProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, zeroStats, Arrays.asList());

            assertEquals(0.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateTimelyProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateTimelyProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.PUNCTUALITY_PROFESSIONAL));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateReliableProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateReliableProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.COMPLETE_DOCUMENTER));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateReliableProgress_PerfectDocumentation_Returns100() {
        DoctorBadgeStatistics perfectStats = DoctorBadgeStatistics.builder()
                .last50DocumentedCount(50)
                .totalTurnsCompleted(50)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateReliableProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, perfectStats, Arrays.asList());

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateFlexibleProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateFlexibleProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.CONSISTENT_PROFESSIONAL));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateFlexibleProgress_HighCancellationRate_ReturnsLowProgress() {
        DoctorBadgeStatistics badStats = DoctorBadgeStatistics.builder()
                .totalTurnsCancelled(20)
                .totalTurnsCompleted(5)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateFlexibleProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, badStats, Arrays.asList());

            assertTrue(progress < 50.0);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateAgileProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateAgileProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.AGILE_RESPONDER));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateAgileProgress_MaxRequests_Returns100() {
        DoctorBadgeStatistics maxStats = DoctorBadgeStatistics.builder()
                .last10RequestsHandled(10)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateAgileProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, maxStats, Arrays.asList());

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateRelationshipProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateRelationshipProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.RELATIONSHIP_BUILDER));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateRelationshipProgress_MaxValues_Returns100() {
        DoctorBadgeStatistics maxStats = DoctorBadgeStatistics.builder()
                .returningPatientsCount(15)
                .totalUniquePatients(60)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateRelationshipProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, maxStats, Arrays.asList());

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateTopSpecialistProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateTopSpecialistProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.TOP_SPECIALIST));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateTopSpecialistProgress_TopPercentile_Returns100() {
        DoctorBadgeStatistics topStats = DoctorBadgeStatistics.builder()
                .specialtyRankPercentile(5.0)
                .totalTurnsCompleted(150)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateTopSpecialistProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, topStats, Arrays.asList());

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateLegendProgress_WithEarnedBadge_Returns100() {
        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateLegendProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, stats, Arrays.asList(BadgeType.MEDICAL_LEGEND));

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateLegendProgress_MaxValues_Returns100() {
        DoctorBadgeStatistics legendStats = DoctorBadgeStatistics.builder()
                .totalTurnsCompleted(600)
                .last100AvgRating(5.0)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateLegendProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, legendStats, Arrays.asList());

            assertEquals(100.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void updateAfterRatingAdded_TriggerRecalculationAt60Ratings() {
        stats.setTotalRatingsReceived(59);
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        DoctorBadgeStatistics statsAfterIncrement = DoctorBadgeStatistics.builder()
                .doctorId(doctorId)
                .doctor(doctor)
                .totalRatingsReceived(60)
                .last50CommunicationCount(10)
                .last50EmpathyCount(12)
                .last50PunctualityCount(8)
                .totalTurnsCompleted(100)
                .totalTurnsCancelled(5)
                .turnsLast90Days(20)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(45)
                .last30DocumentedCount(25)
                .last30TotalWords(4000)
                .last30AvgWordsPerEntry(160.0)
                .totalUniquePatients(50)
                .returningPatientsCount(8)
                .last10RequestsHandled(5)
                .build();

        com.medibook.api.entity.Rating rating = mock(com.medibook.api.entity.Rating.class);
        when(rating.getScore()).thenReturn(5);
        when(rating.getSubcategory()).thenReturn("comunica claramente");
        when(ratingRepository.findTop50ByRatedIdOrderByCreatedAtDesc(doctorId))
            .thenReturn(Arrays.asList(rating));

        when(statisticsRepository.findByDoctorId(doctorId))
                .thenReturn(Optional.of(stats))
                .thenReturn(Optional.of(statsAfterIncrement));

        updateService.updateAfterRatingAdded(doctorId, 4, 5, 4);

        verify(statisticsRepository).incrementRatingCounters(doctorId, true, true, true);
        verify(ratingRepository).findTop50ByRatedIdOrderByCreatedAtDesc(doctorId);
    }

    @Test
    void updateAfterRatingAdded_TriggerRecalculationAt70Ratings() {
        stats.setTotalRatingsReceived(69);
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        DoctorBadgeStatistics statsAfterIncrement = DoctorBadgeStatistics.builder()
                .doctorId(doctorId)
                .doctor(doctor)
                .totalRatingsReceived(70)
                .last50CommunicationCount(10)
                .last50EmpathyCount(12)
                .last50PunctualityCount(8)
                .totalTurnsCompleted(100)
                .totalTurnsCancelled(5)
                .turnsLast90Days(20)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(45)
                .last30DocumentedCount(25)
                .last30TotalWords(4000)
                .last30AvgWordsPerEntry(160.0)
                .totalUniquePatients(50)
                .returningPatientsCount(8)
                .last10RequestsHandled(5)
                .build();

        com.medibook.api.entity.Rating rating = mock(com.medibook.api.entity.Rating.class);
        when(rating.getScore()).thenReturn(5);
        when(rating.getSubcategory()).thenReturn("comunica claramente");
        when(ratingRepository.findTop50ByRatedIdOrderByCreatedAtDesc(doctorId))
            .thenReturn(Arrays.asList(rating));

        when(statisticsRepository.findByDoctorId(doctorId))
                .thenReturn(Optional.of(stats))
                .thenReturn(Optional.of(statsAfterIncrement));

        updateService.updateAfterRatingAdded(doctorId, 4, 5, 4);

        verify(statisticsRepository).incrementRatingCounters(doctorId, true, true, true);
        verify(ratingRepository).findTop50ByRatedIdOrderByCreatedAtDesc(doctorId);
    }

    @Test
    void updateAfterTurnCompleted_ExceptionDuringPatientStatsUpdate_LogsError() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED"))
                .thenReturn(1L);

        doThrow(new RuntimeException("Database error")).when(statisticsRepository).save(any());

        updateService.updateAfterTurnCompleted(doctorId, patientId);

        verify(statisticsRepository).incrementTurnCompleted(doctorId);
        verify(turnAssignedRepository).countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED");
    }

    @Test
    void updateAfterMedicalHistoryDocumented_EmptyContent_UpdatesWithZeroWords() {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        updateService.updateAfterMedicalHistoryDocumented(doctorId, null);

        verify(statisticsRepository).incrementDocumentation(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(4000, savedStats.getLast30TotalWords());
    }

    @Test
    void updateAfterMedicalHistoryDocumented_LongContent_UpdatesWordCount() {
        String longContent = "This is a very long medical history entry with many words that should be counted properly by the system. " +
                            "It contains multiple sentences and detailed information about the patient's condition. " +
                            "The word count should be calculated accurately.";

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        updateService.updateAfterMedicalHistoryDocumented(doctorId, longContent);

        verify(statisticsRepository).incrementDocumentation(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(4037, savedStats.getLast30TotalWords());
        assertEquals(161.48, savedStats.getLast30AvgWordsPerEntry(), 0.01);
    }

    @Test
    void recalculateLast50Ratings_WithCommunicationKeywords_IncrementsCount() {
        com.medibook.api.entity.Rating rating1 = mock(com.medibook.api.entity.Rating.class);
        when(rating1.getScore()).thenReturn(5);
        when(rating1.getSubcategory()).thenReturn("explica claramente");

        com.medibook.api.entity.Rating rating2 = mock(com.medibook.api.entity.Rating.class);
        when(rating2.getScore()).thenReturn(4);
        when(rating2.getSubcategory()).thenReturn("escucha atentamente");

        when(ratingRepository.findTop50ByRatedIdOrderByCreatedAtDesc(doctorId))
            .thenReturn(Arrays.asList(rating1, rating2));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("recalculateLast50Ratings", UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(2, savedStats.getLast50CommunicationCount());
        assertEquals(0, savedStats.getLast50EmpathyCount());
        assertEquals(0, savedStats.getLast50PunctualityCount());
    }

    @Test
    void recalculateLast50Ratings_WithEmpathyKeywords_IncrementsCount() {
        com.medibook.api.entity.Rating rating1 = mock(com.medibook.api.entity.Rating.class);
        when(rating1.getScore()).thenReturn(5);
        when(rating1.getSubcategory()).thenReturn("muestra empatía");

        com.medibook.api.entity.Rating rating2 = mock(com.medibook.api.entity.Rating.class);
        when(rating2.getScore()).thenReturn(4);
        when(rating2.getSubcategory()).thenReturn("genera confianza");

        when(ratingRepository.findTop50ByRatedIdOrderByCreatedAtDesc(doctorId))
            .thenReturn(Arrays.asList(rating1, rating2));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("recalculateLast50Ratings", UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(0, savedStats.getLast50CommunicationCount());
        assertEquals(2, savedStats.getLast50EmpathyCount());
        assertEquals(0, savedStats.getLast50PunctualityCount());
    }

    @Test
    void recalculateLast50Ratings_WithPunctualityKeywords_IncrementsCount() {
        com.medibook.api.entity.Rating rating1 = mock(com.medibook.api.entity.Rating.class);
        when(rating1.getScore()).thenReturn(5);
        when(rating1.getSubcategory()).thenReturn("respeta horarios");

        com.medibook.api.entity.Rating rating2 = mock(com.medibook.api.entity.Rating.class);
        when(rating2.getScore()).thenReturn(4);
        when(rating2.getSubcategory()).thenReturn("es puntual");

        when(ratingRepository.findTop50ByRatedIdOrderByCreatedAtDesc(doctorId))
            .thenReturn(Arrays.asList(rating1, rating2));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("recalculateLast50Ratings", UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(0, savedStats.getLast50CommunicationCount());
        assertEquals(0, savedStats.getLast50EmpathyCount());
        assertEquals(2, savedStats.getLast50PunctualityCount());
    }

    @Test
    void recalculateLast50Ratings_MixedKeywords_IncrementsCorrectly() {
        com.medibook.api.entity.Rating commRating = mock(com.medibook.api.entity.Rating.class);
        when(commRating.getScore()).thenReturn(5);
        when(commRating.getSubcategory()).thenReturn("explica claramente");

        com.medibook.api.entity.Rating empRating = mock(com.medibook.api.entity.Rating.class);
        when(empRating.getScore()).thenReturn(4);
        when(empRating.getSubcategory()).thenReturn("muestra empatía");

        com.medibook.api.entity.Rating punctRating = mock(com.medibook.api.entity.Rating.class);
        when(punctRating.getScore()).thenReturn(5);
        when(punctRating.getSubcategory()).thenReturn("respeta horarios");

        com.medibook.api.entity.Rating lowScoreRating = mock(com.medibook.api.entity.Rating.class);
        when(lowScoreRating.getScore()).thenReturn(3);

        when(ratingRepository.findTop50ByRatedIdOrderByCreatedAtDesc(doctorId))
            .thenReturn(Arrays.asList(commRating, empRating, punctRating, lowScoreRating));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("recalculateLast50Ratings", UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(1, savedStats.getLast50CommunicationCount());
        assertEquals(1, savedStats.getLast50EmpathyCount());
        assertEquals(1, savedStats.getLast50PunctualityCount());
    }

    @Test
    void recalculateLast50Ratings_ExceptionDuringProcess_LogsError() {
        when(ratingRepository.findTop50ByRatedIdOrderByCreatedAtDesc(doctorId))
            .thenThrow(new RuntimeException("Database error"));

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("recalculateLast50Ratings", UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        verify(ratingRepository).findTop50ByRatedIdOrderByCreatedAtDesc(doctorId);
        verify(statisticsRepository, never()).save(any());
    }

    @Test
    void updatePatientRelationshipStats_FirstTurnWithPatient_IncrementsUnique() {
        when(turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED"))
                .thenReturn(1L);

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("updatePatientRelationshipStats", UUID.class, UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId, patientId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(51, savedStats.getTotalUniquePatients());
        assertEquals(8, savedStats.getReturningPatientsCount());
    }

    @Test
    void updatePatientRelationshipStats_SecondTurnWithPatient_IncrementsReturning() {
        when(turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED"))
                .thenReturn(2L);

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("updatePatientRelationshipStats", UUID.class, UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId, patientId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(50, savedStats.getTotalUniquePatients());
        assertEquals(9, savedStats.getReturningPatientsCount());
    }

    @Test
    void updatePatientRelationshipStats_ExceptionDuringUpdate_LogsError() {
        when(turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED"))
                .thenReturn(1L);
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        doThrow(new RuntimeException("Database error")).when(statisticsRepository).save(any());

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("updatePatientRelationshipStats", UUID.class, UUID.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId, patientId);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        verify(turnAssignedRepository).countByDoctorIdAndPatientIdAndStatus(doctorId, patientId, "COMPLETED");
        verify(statisticsRepository).findByDoctorId(doctorId);
    }

    @Test
    void updateWordCountStatistics_ZeroDocumentedCount_HandlesDivision() {
        DoctorBadgeStatistics zeroDocStats = DoctorBadgeStatistics.builder()
                .last30TotalWords(100)
                .last30DocumentedCount(0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(zeroDocStats));

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("updateWordCountStatistics", UUID.class, int.class);
            method.setAccessible(true);
            method.invoke(updateService, doctorId, 50);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(150, savedStats.getLast30TotalWords());
        assertEquals(0.0, savedStats.getLast30AvgWordsPerEntry());
    }

    @Test
    void updateAllBadgeProgress_CalculatesAllProgressWithEarnedBadges() {
        List<BadgeType> earnedBadges = Arrays.asList(
            BadgeType.SUSTAINED_EXCELLENCE,
            BadgeType.EMPATHETIC_DOCTOR,
            BadgeType.EXCEPTIONAL_COMMUNICATOR,
            BadgeType.COMPLETE_DOCUMENTER,
            BadgeType.DETAILED_HISTORIAN,
            BadgeType.PUNCTUALITY_PROFESSIONAL,
            BadgeType.CONSISTENT_PROFESSIONAL,
            BadgeType.AGILE_RESPONDER,
            BadgeType.RELATIONSHIP_BUILDER,
            BadgeType.TOP_SPECIALIST,
            BadgeType.MEDICAL_LEGEND,
            BadgeType.ALWAYS_AVAILABLE
        );

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        List<DoctorBadge> earnedBadgesList = earnedBadges.stream()
                .map(badgeType -> {
                    DoctorBadge badge = new DoctorBadge();
                    badge.setBadgeType(badgeType);
                    return badge;
                })
                .toList();
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId))
                .thenReturn(earnedBadgesList);

        updateService.updateAllBadgeProgress(doctorId);

        ArgumentCaptor<DoctorBadgeStatistics> captor = ArgumentCaptor.forClass(DoctorBadgeStatistics.class);
        verify(statisticsRepository).save(captor.capture());

        DoctorBadgeStatistics savedStats = captor.getValue();
        assertEquals(100.0, savedStats.getProgressExcellenceInCare());
        assertEquals(100.0, savedStats.getProgressEmpathyChampion());
        assertEquals(100.0, savedStats.getProgressClearCommunicator());
        assertEquals(100.0, savedStats.getProgressDetailedDiagnostician());
        assertEquals(100.0, savedStats.getProgressTimelyProfessional());
        assertEquals(100.0, savedStats.getProgressReliableExpert());
        assertEquals(100.0, savedStats.getProgressFlexibleCaregiver());
        assertEquals(100.0, savedStats.getProgressAgileResponder());
        assertEquals(100.0, savedStats.getProgressRelationshipBuilder());
        assertEquals(100.0, savedStats.getProgressTopSpecialist());
        assertEquals(100.0, savedStats.getProgressMedicalLegend());
        assertEquals(100.0, savedStats.getProgressAllStarDoctor());
    }

    @Test
    void calculateExcellenceProgress_NoLast100AvgRating_UsesZero() {
        DoctorBadgeStatistics noRatingStats = DoctorBadgeStatistics.builder()
                .totalRatingsReceived(150)
                .last100AvgRating(null)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateExcellenceProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, noRatingStats, Arrays.asList());

            assertEquals(50.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateFlexibleProgress_ZeroCompletedTurns_ReturnsZero() {
        DoctorBadgeStatistics zeroCompletedStats = DoctorBadgeStatistics.builder()
                .totalTurnsCancelled(10)
                .totalTurnsCompleted(0)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateFlexibleProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, zeroCompletedStats, Arrays.asList());

            assertEquals(0.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateTopSpecialistProgress_NoPercentile_ReturnsZero() {
        DoctorBadgeStatistics noPercentileStats = DoctorBadgeStatistics.builder()
                .specialtyRankPercentile(null)
                .totalTurnsCompleted(150)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateTopSpecialistProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, noPercentileStats, Arrays.asList());

            assertEquals(50.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }

    @Test
    void calculateLegendProgress_NoLast100AvgRating_UsesZero() {
        DoctorBadgeStatistics noRatingStats = DoctorBadgeStatistics.builder()
                .totalTurnsCompleted(600)
                .last100AvgRating(null)
                .build();

        try {
            Method method = DoctorBadgeStatisticsUpdateService.class
                .getDeclaredMethod("calculateLegendProgress", DoctorBadgeStatistics.class, List.class);
            method.setAccessible(true);

            double progress = (Double) method.invoke(updateService, noRatingStats, Arrays.asList());

            assertEquals(50.0, progress);
        } catch (Exception e) {
            fail("Reflection call failed: " + e.getMessage());
        }
    }
}