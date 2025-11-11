package com.medibook.api.service;

import com.medibook.api.dto.Badge.BadgeDTO;
import com.medibook.api.dto.Badge.DoctorBadgesResponseDTO;
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
class DoctorBadgeServiceTest {

    @Mock
    private DoctorBadgeRepository badgeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorBadgeStatisticsRepository statisticsRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private TurnAssignedRepository turnAssignedRepository;

    @InjectMocks
    private DoctorBadgeService doctorBadgeService;

    private UUID doctorId;
    private User doctor;
    private DoctorBadgeStatistics stats;
    private DoctorProfile doctorProfile;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        doctor = new User();
        doctor.setId(doctorId);
        doctor.setName("Dr. Test");
        doctor.setSurname("Doctor");
        doctor.setRole("DOCTOR");
        doctor.setStatus("ACTIVE");
        doctor.setScore(4.8);

        stats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(200)
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        doctorProfile = new DoctorProfile();
        doctorProfile.setId(doctorId);
        doctorProfile.setSpecialty("Cardiology");
        doctorProfile.setAvailabilitySchedule("{\"monday\": \"09:00-17:00\"}");
    }

    @Test
    void getDoctorBadges_DoctorFound_ReturnsBadgesGroupedByCategory() {
        DoctorBadge activeBadge1 = createBadge(BadgeType.SUSTAINED_EXCELLENCE, true);
        DoctorBadge activeBadge2 = createBadge(BadgeType.EMPATHETIC_DOCTOR, true);
        DoctorBadge activeBadge3 = createBadge(BadgeType.EXCEPTIONAL_COMMUNICATOR, true);
        DoctorBadge activeBadge4 = createBadge(BadgeType.CONSISTENT_PROFESSIONAL, true);

        List<DoctorBadge> allBadges = Arrays.asList(activeBadge1, activeBadge2, activeBadge3, activeBadge4);

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(badgeRepository.findByDoctor_IdOrderByEarnedAtDesc(any())).thenReturn(new ArrayList<>());
        when(badgeRepository.findByDoctor_IdOrderByEarnedAtDesc(eq(doctorId))).thenReturn(allBadges);

        DoctorBadgesResponseDTO result = doctorBadgeService.getDoctorBadges(doctorId);

        assertNotNull(result);
        assertEquals(doctorId, result.getDoctorId());
        assertEquals("Dr. Test Doctor", result.getDoctorName());
        assertEquals(4, result.getTotalActiveBadges()); 

        long totalCategories = 0;
        if (!result.getQualityOfCareBadges().isEmpty()) totalCategories++;
        if (!result.getProfessionalismBadges().isEmpty()) totalCategories++;
        if (!result.getConsistencyBadges().isEmpty()) totalCategories++;
        assertEquals(2, totalCategories);

        assertEquals(3, result.getQualityOfCareBadges().size());
        assertTrue(result.getQualityOfCareBadges().stream()
                .anyMatch(b -> b.getBadgeType() == BadgeType.SUSTAINED_EXCELLENCE && b.getIsActive()));
        assertTrue(result.getQualityOfCareBadges().stream()
                .anyMatch(b -> b.getBadgeType() == BadgeType.EMPATHETIC_DOCTOR && b.getIsActive()));
        assertTrue(result.getQualityOfCareBadges().stream()
                .anyMatch(b -> b.getBadgeType() == BadgeType.EXCEPTIONAL_COMMUNICATOR && b.getIsActive()));

        assertEquals(0, result.getProfessionalismBadges().size());

        assertEquals(1, result.getConsistencyBadges().size());
        assertTrue(result.getConsistencyBadges().stream()
                .anyMatch(b -> b.getBadgeType() == BadgeType.CONSISTENT_PROFESSIONAL && b.getIsActive()));

        verify(userRepository).findById(doctorId);
        verify(badgeRepository).findByDoctor_IdOrderByEarnedAtDesc(doctorId);
    }

    @Test
    void getDoctorBadges_DoctorNotFound_ThrowsException() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> doctorBadgeService.getDoctorBadges(doctorId));

        assertEquals("Doctor not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void getDoctorBadges_UserNotDoctor_ThrowsException() {
        User patient = new User();
        patient.setId(doctorId);
        patient.setRole("PATIENT");

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(patient));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> doctorBadgeService.getDoctorBadges(doctorId));

        assertEquals("User is not a doctor", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void evaluateAllBadges_DoctorFound_EvaluatesAllBadgeTypes() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(doctorProfileRepository.findByUserId(doctorId)).thenReturn(Optional.of(doctorProfile));

        List<User> specialtyDoctors = Arrays.asList(doctor);
        lenient().when(userRepository.findDoctorsByStatusAndSpecialty("ACTIVE", "Cardiology"))
                .thenReturn(specialtyDoctors);

        List<TurnAssigned> turns = createMockTurns(10);
        when(turnAssignedRepository.findByDoctor_IdAndStatusOrderByScheduledAtDesc(eq(doctorId), anyString()))
                .thenReturn(turns);

        Rating rating = new Rating();
        rating.setScore(5);
        when(ratingRepository.findByTurnAssigned_IdAndRater_Id(any(), any()))
                .thenReturn(Optional.of(rating));

        doctorBadgeService.evaluateAllBadges(doctorId);

        verify(badgeRepository, atLeast(12)).findByDoctor_IdAndBadgeType(any(), any());

        verify(userRepository).findById(doctorId);
        verify(statisticsRepository, atLeastOnce()).findByDoctorId(doctorId);
    }

    @Test
    void evaluateAllBadges_DoctorNotFound_ThrowsException() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> doctorBadgeService.evaluateAllBadges(doctorId));

        assertEquals("Doctor not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
    }

    @Test
    void evaluateAllBadges_UserNotDoctor_ThrowsException() {
        User patient = new User();
        patient.setId(doctorId);
        patient.setRole("PATIENT");

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(patient));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> doctorBadgeService.evaluateAllBadges(doctorId));

        assertEquals("User is not a doctor", exception.getMessage());
        verify(userRepository).findById(doctorId);
    }

    @Test
    void evaluateRatingRelatedBadges_EvaluatesCorrectBadgeTypes() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        List<TurnAssigned> turns = createMockTurns(10);
        when(turnAssignedRepository.findByDoctor_IdAndStatusOrderByScheduledAtDesc(eq(doctorId), anyString()))
                .thenReturn(turns);

        Rating rating = new Rating();
        rating.setScore(5);
        when(ratingRepository.findByTurnAssigned_IdAndRater_Id(any(), any()))
                .thenReturn(Optional.of(rating));

        doctorBadgeService.evaluateRatingRelatedBadges(doctorId);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.EXCEPTIONAL_COMMUNICATOR);
        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.EMPATHETIC_DOCTOR);
        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.SUSTAINED_EXCELLENCE);

        verify(badgeRepository, never()).findByDoctor_IdAndBadgeType(doctorId, BadgeType.COMPLETE_DOCUMENTER);
        verify(badgeRepository, never()).findByDoctor_IdAndBadgeType(doctorId, BadgeType.RELATIONSHIP_BUILDER);
    }

    @Test
    void evaluateDocumentationRelatedBadges_EvaluatesCorrectBadgeTypes() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        doctorBadgeService.evaluateDocumentationRelatedBadges(doctorId);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.COMPLETE_DOCUMENTER);
        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.DETAILED_HISTORIAN);

        verify(badgeRepository, never()).findByDoctor_IdAndBadgeType(doctorId, BadgeType.EXCEPTIONAL_COMMUNICATOR);
        verify(badgeRepository, never()).findByDoctor_IdAndBadgeType(doctorId, BadgeType.RELATIONSHIP_BUILDER);
    }

    @Test
    void evaluateConsistencyRelatedBadges_EvaluatesCorrectBadgeTypes() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        doctorBadgeService.evaluateConsistencyRelatedBadges(doctorId);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.CONSISTENT_PROFESSIONAL);

        verify(badgeRepository, never()).findByDoctor_IdAndBadgeType(doctorId, BadgeType.EXCEPTIONAL_COMMUNICATOR);
        verify(badgeRepository, never()).findByDoctor_IdAndBadgeType(doctorId, BadgeType.COMPLETE_DOCUMENTER);
    }

    @Test
    void evaluateResponseRelatedBadges_EvaluatesCorrectBadgeTypes() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        doctorBadgeService.evaluateResponseRelatedBadges(doctorId);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.AGILE_RESPONDER);

        verify(badgeRepository, never()).findByDoctor_IdAndBadgeType(doctorId, BadgeType.EXCEPTIONAL_COMMUNICATOR);
        verify(badgeRepository, never()).findByDoctor_IdAndBadgeType(doctorId, BadgeType.COMPLETE_DOCUMENTER);
    }

    @Test
    void evaluateTurnCompletionRelatedBadges_EvaluatesCorrectBadgeTypes() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(doctorProfileRepository.findByUserId(doctorId)).thenReturn(Optional.of(doctorProfile));

        List<User> specialtyDoctors = Arrays.asList(doctor);
        lenient().when(userRepository.findDoctorsByStatusAndSpecialty("ACTIVE", "Cardiology"))
                .thenReturn(specialtyDoctors);

        List<TurnAssigned> turns = createMockTurns(10);
        lenient().when(turnAssignedRepository.findByDoctor_IdAndStatusOrderByScheduledAtDesc(eq(doctorId), anyString()))
                .thenReturn(turns);

        Rating rating = new Rating();
        rating.setScore(5);
        lenient().when(ratingRepository.findByTurnAssigned_IdAndRater_Id(any(), any()))
                .thenReturn(Optional.of(rating));

        doctorBadgeService.evaluateTurnCompletionRelatedBadges(doctorId);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.RELATIONSHIP_BUILDER);
        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.TOP_SPECIALIST);
        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.MEDICAL_LEGEND);

        verify(badgeRepository, never()).findByDoctor_IdAndBadgeType(doctorId, BadgeType.EXCEPTIONAL_COMMUNICATOR);
        verify(badgeRepository, never()).findByDoctor_IdAndBadgeType(doctorId, BadgeType.COMPLETE_DOCUMENTER);
    }

    @Test
    void evaluatePunctualityProfessional_LowPunctualityRatings_DeactivatesBadge() {
        stats.setTotalRatingsReceived(50);
        stats.setTurnsLast90Days(30);
        stats.setLast50PunctualityCount(500); 
        stats.setCancellationsLast90Days(1); 

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        doctorBadgeService.evaluateAllBadges(doctorId);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.PUNCTUALITY_PROFESSIONAL);
        verify(badgeRepository, atLeastOnce()).save(any(DoctorBadge.class)); 
    }

    @Test
    void evaluatePunctualityProfessional_HighCancellationRate_DeactivatesBadge_Alt() {
        stats.setTotalRatingsReceived(50);
        stats.setTurnsLast90Days(30);
        stats.setLast50PunctualityCount(650); 
        stats.setCancellationsLast90Days(5); 

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));

        doctorBadgeService.evaluateAllBadges(doctorId);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.PUNCTUALITY_PROFESSIONAL);
        verify(badgeRepository, atLeastOnce()).save(any(DoctorBadge.class)); 
    }

    @Test
    void evaluatePunctualityProfessional_ZeroTurnsLast90Days_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics zeroTurnsStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(50)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(200)
                .totalTurnsCancelled(5)
                .turnsLast90Days(0) 
                .cancellationsLast90Days(0)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(zeroTurnsStats));

        DoctorBadge existingBadge = createBadge(BadgeType.PUNCTUALITY_PROFESSIONAL, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.PUNCTUALITY_PROFESSIONAL))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluatePunctualityProfessional", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.PUNCTUALITY_PROFESSIONAL);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateRelationshipBuilder_InsufficientPatients_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics lowPatientStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(200)
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(30) 
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(lowPatientStats));

        DoctorBadge existingBadge = createBadge(BadgeType.RELATIONSHIP_BUILDER, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.RELATIONSHIP_BUILDER))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateRelationshipBuilder", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.RELATIONSHIP_BUILDER);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateRelationshipBuilder_InsufficientReturningPatients_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics lowReturningStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(200)
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(5) 
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(lowReturningStats));

        DoctorBadge existingBadge = createBadge(BadgeType.RELATIONSHIP_BUILDER, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.RELATIONSHIP_BUILDER))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateRelationshipBuilder", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.RELATIONSHIP_BUILDER);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateConsistentProfessional_HighCancellationRate_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics highCancellationStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(200)
                .totalTurnsCancelled(5)
                .turnsLast90Days(20)
                .cancellationsLast90Days(5) 
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(highCancellationStats));

        DoctorBadge existingBadge = createBadge(BadgeType.CONSISTENT_PROFESSIONAL, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.CONSISTENT_PROFESSIONAL))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateConsistentProfessional", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.CONSISTENT_PROFESSIONAL);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateAlwaysAvailable_NoDoctorProfile_DeactivatesBadge() throws Exception {
        when(doctorProfileRepository.findByUserId(doctorId)).thenReturn(Optional.empty());

        DoctorBadge existingBadge = createBadge(BadgeType.ALWAYS_AVAILABLE, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.ALWAYS_AVAILABLE))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateAlwaysAvailable", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.ALWAYS_AVAILABLE);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateAlwaysAvailable_EmptyAvailabilitySchedule_DeactivatesBadge() throws Exception {
        DoctorProfile emptyScheduleProfile = new DoctorProfile();
        emptyScheduleProfile.setId(doctorId);
        emptyScheduleProfile.setSpecialty("Cardiology");
        emptyScheduleProfile.setAvailabilitySchedule(""); 

        when(doctorProfileRepository.findByUserId(doctorId)).thenReturn(Optional.of(emptyScheduleProfile));

        DoctorBadge existingBadge = createBadge(BadgeType.ALWAYS_AVAILABLE, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.ALWAYS_AVAILABLE))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateAlwaysAvailable", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.ALWAYS_AVAILABLE);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateAgileResponder_InsufficientRequests_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics lowRequestsStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(200)
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(5) 
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(lowRequestsStats));

        DoctorBadge existingBadge = createBadge(BadgeType.AGILE_RESPONDER, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.AGILE_RESPONDER))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateAgileResponder", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.AGILE_RESPONDER);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateSustainedExcellence_NoRatings_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics noRatingsStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(150) 
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(noRatingsStats));

        List<TurnAssigned> turns = createMockTurns(100);
        when(turnAssignedRepository.findByDoctor_IdAndStatusOrderByScheduledAtDesc(eq(doctorId), eq("COMPLETED")))
            .thenReturn(turns);
        when(ratingRepository.findByTurnAssigned_IdAndRater_Id(any(), any())).thenReturn(Optional.empty());

        DoctorBadge existingBadge = createBadge(BadgeType.SUSTAINED_EXCELLENCE, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.SUSTAINED_EXCELLENCE))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateSustainedExcellence", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.SUSTAINED_EXCELLENCE);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateSustainedExcellence_LowAverageScore_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics lowScoreStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(150)
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(lowScoreStats));

        List<TurnAssigned> turns = createMockTurns(100);
        when(turnAssignedRepository.findByDoctor_IdAndStatusOrderByScheduledAtDesc(eq(doctorId), eq("COMPLETED")))
            .thenReturn(turns);

        for (TurnAssigned turn : turns) {
            Rating lowRating = new Rating();
            lowRating.setScore(3); 
            when(ratingRepository.findByTurnAssigned_IdAndRater_Id(turn.getId(), turn.getPatient().getId()))
                .thenReturn(Optional.of(lowRating));
        }

        DoctorBadge existingBadge = createBadge(BadgeType.SUSTAINED_EXCELLENCE, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.SUSTAINED_EXCELLENCE))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateSustainedExcellence", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.SUSTAINED_EXCELLENCE);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void getOrCreateStatistics_NoExistingStatistics_CreatesDefaultStatistics() throws Exception {
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.empty());
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.save(any(DoctorBadgeStatistics.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Method method = DoctorBadgeService.class.getDeclaredMethod("getOrCreateStatistics", UUID.class);
        method.setAccessible(true);
        DoctorBadgeStatistics result = (DoctorBadgeStatistics) method.invoke(doctorBadgeService, doctorId);

        assertNotNull(result);
        assertEquals(doctor, result.getDoctor());
        assertEquals(0, result.getTotalRatingsReceived());
        assertEquals(0, result.getLast50CommunicationCount());
        assertEquals(0, result.getLast50EmpathyCount());
        assertEquals(0, result.getLast50PunctualityCount());
        assertEquals(0, result.getTotalTurnsCompleted());
        assertEquals(0, result.getTotalTurnsCancelled());
        assertEquals(0, result.getTurnsLast90Days());
        assertEquals(0, result.getCancellationsLast90Days());
        assertEquals(0, result.getLast50DocumentedCount());
        assertEquals(0, result.getLast30DocumentedCount());
        assertEquals(0, result.getLast30TotalWords());
        assertEquals(0.0, result.getLast30AvgWordsPerEntry());
        assertEquals(0, result.getTotalUniquePatients());
        assertEquals(0, result.getReturningPatientsCount());
        assertEquals(0, result.getLast10RequestsHandled());
        assertNull(result.getSpecialtyRankPercentile());

        verify(statisticsRepository).save(any(DoctorBadgeStatistics.class));
    }

    @Test
    void evaluateExceptionalCommunicator_InsufficientRatings_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics lowRatingStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(30) 
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(200)
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(lowRatingStats));

        DoctorBadge existingBadge = createBadge(BadgeType.EXCEPTIONAL_COMMUNICATOR, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.EXCEPTIONAL_COMMUNICATOR))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateExceptionalCommunicator", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.EXCEPTIONAL_COMMUNICATOR);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateEmpatheticDoctor_InsufficientRatings_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics lowRatingStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(30) 
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(200)
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(lowRatingStats));

        DoctorBadge existingBadge = createBadge(BadgeType.EMPATHETIC_DOCTOR, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.EMPATHETIC_DOCTOR))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateEmpatheticDoctor", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.EMPATHETIC_DOCTOR);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateCompleteDocumenter_InsufficientTurns_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics lowTurnStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(30) 
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(lowTurnStats));

        DoctorBadge existingBadge = createBadge(BadgeType.COMPLETE_DOCUMENTER, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.COMPLETE_DOCUMENTER))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateCompleteDocumenter", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.COMPLETE_DOCUMENTER);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateDetailedHistorian_InsufficientTurns_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics lowTurnStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(20) 
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(lowTurnStats));

        DoctorBadge existingBadge = createBadge(BadgeType.DETAILED_HISTORIAN, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.DETAILED_HISTORIAN))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateDetailedHistorian", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.DETAILED_HISTORIAN);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateDetailedHistorian_MissingPrerequisite_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics goodDocStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(50)
                .totalTurnsCancelled(5)
                .turnsLast90Days(25)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(30) 
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(goodDocStats));
        when(badgeRepository.existsByDoctor_IdAndBadgeTypeAndIsActive(doctorId, BadgeType.COMPLETE_DOCUMENTER, true))
            .thenReturn(false);

        DoctorBadge existingBadge = createBadge(BadgeType.DETAILED_HISTORIAN, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.DETAILED_HISTORIAN))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateDetailedHistorian", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.DETAILED_HISTORIAN);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    @Test
    void evaluateConsistentProfessional_InsufficientTurns_DeactivatesBadge() throws Exception {
        DoctorBadgeStatistics lowTurnStats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(200)
                .totalTurnsCancelled(5)
                .turnsLast90Days(15) 
                .cancellationsLast90Days(2)
                .last50DocumentedCount(48)
                .last30DocumentedCount(28)
                .last30TotalWords(4500)
                .last30AvgWordsPerEntry(160.7)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(10)
                .specialtyRankPercentile(5.0)
                .build();

        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(lowTurnStats));

        DoctorBadge existingBadge = createBadge(BadgeType.CONSISTENT_PROFESSIONAL, true);
        when(badgeRepository.findByDoctor_IdAndBadgeType(doctorId, BadgeType.CONSISTENT_PROFESSIONAL))
            .thenReturn(Optional.of(existingBadge));

        Method method = DoctorBadgeService.class.getDeclaredMethod("evaluateConsistentProfessional", User.class);
        method.setAccessible(true);
        method.invoke(doctorBadgeService, doctor);

        verify(badgeRepository).findByDoctor_IdAndBadgeType(doctorId, BadgeType.CONSISTENT_PROFESSIONAL);
        verify(badgeRepository).save(existingBadge);
        assertFalse(existingBadge.getIsActive());
    }

    private List<TurnAssigned> createMockTurns(int count) {
        List<TurnAssigned> turns = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TurnAssigned turn = new TurnAssigned();
            turn.setId(UUID.randomUUID());
            turn.setScheduledAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(i));
            turn.setStatus("COMPLETED");

            User patient = new User();
            patient.setId(UUID.randomUUID());
            turn.setPatient(patient);

            turns.add(turn);
        }
        return turns;
    }

    private DoctorBadge createBadge(BadgeType badgeType, boolean isActive) {
        DoctorBadge badge = new DoctorBadge();
        badge.setBadgeType(badgeType);
        badge.setIsActive(isActive);
        badge.setEarnedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(30));
        badge.setLastEvaluatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return badge;
    }

    private User createDoctorWithScore(double score) {
        User doctorWithScore = new User();
        doctorWithScore.setId(UUID.randomUUID());
        doctorWithScore.setName("Dr. Scored");
        doctorWithScore.setSurname("Doctor");
        doctorWithScore.setRole("DOCTOR");
        doctorWithScore.setStatus("ACTIVE");
        doctorWithScore.setScore(score);
        return doctorWithScore;
    }
}