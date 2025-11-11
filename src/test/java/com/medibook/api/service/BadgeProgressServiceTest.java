package com.medibook.api.service;

import com.medibook.api.dto.Badge.BadgeProgressSummaryDTO;
import com.medibook.api.entity.BadgeType;
import com.medibook.api.entity.BadgeType.BadgeCategory;
import com.medibook.api.entity.DoctorBadge;
import com.medibook.api.entity.DoctorBadgeStatistics;
import com.medibook.api.entity.User;
import com.medibook.api.repository.DoctorBadgeRepository;
import com.medibook.api.repository.DoctorBadgeStatisticsRepository;
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
class BadgeProgressServiceTest {

    @Mock
    private DoctorBadgeStatisticsRepository statisticsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorBadgeRepository badgeRepository;

    @InjectMocks
    private BadgeProgressService badgeProgressService;

    private UUID doctorId;
    private User doctor;
    private DoctorBadgeStatistics stats;
    private List<DoctorBadge> earnedBadges;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        doctor = new User();
        doctor.setId(doctorId);
        doctor.setName("Dr. Test");
        doctor.setSurname("Doctor");
        doctor.setRole("DOCTOR");
        doctor.setStatus("ACTIVE");

        stats = DoctorBadgeStatistics.builder()
                .doctorId(doctorId)
                .totalRatingsReceived(100)
                .last50CommunicationCount(20)
                .last50EmpathyCount(18)
                .last50PunctualityCount(15)
                .totalTurnsCompleted(200)
                .totalTurnsCancelled(5)
                .last30DocumentedCount(25)
                .totalUniquePatients(80)
                .returningPatientsCount(15)
                .last10RequestsHandled(8)
                .progressExcellenceInCare(85.0)
                .progressEmpathyChampion(90.0)
                .progressClearCommunicator(75.0)
                .progressDetailedDiagnostician(60.0)
                .progressTimelyProfessional(80.0)
                .progressReliableExpert(95.0)
                .progressFlexibleCaregiver(88.0)
                .progressAgileResponder(70.0)
                .progressRelationshipBuilder(65.0)
                .progressTopSpecialist(92.0)
                .progressMedicalLegend(78.0)
                .progressAllStarDoctor(0.0)
                .build();

        DoctorBadge badge1 = new DoctorBadge();
        badge1.setBadgeType(BadgeType.SUSTAINED_EXCELLENCE);
        badge1.setIsActive(true);

        DoctorBadge badge2 = new DoctorBadge();
        badge2.setBadgeType(BadgeType.EMPATHETIC_DOCTOR);
        badge2.setIsActive(true);

        earnedBadges = Arrays.asList(badge1, badge2);
    }

    @Test
    void getBadgeProgress_DoctorFoundWithStatsAndBadges_ReturnsCompleteProgressList() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId)).thenReturn(earnedBadges);

        List<BadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(doctorId);

        assertEquals(12, result.size());

        BadgeProgressSummaryDTO excellenceBadge = result.get(0);
        assertEquals(BadgeType.SUSTAINED_EXCELLENCE, excellenceBadge.getBadgeType());
        assertEquals("Excelencia Sostenida", excellenceBadge.getBadgeName());
        assertEquals(BadgeCategory.QUALITY_OF_CARE, excellenceBadge.getCategory());
        assertEquals(85.0, excellenceBadge.getProgressPercentage());
        assertTrue(excellenceBadge.getEarned());
        assertEquals("Mantén un promedio de 4.7⭐ en los últimos 100 turnos", excellenceBadge.getDescription());
        assertEquals("¡Insignia obtenida! Excelente trabajo.", excellenceBadge.getStatusMessage());

        BadgeProgressSummaryDTO empathyBadge = result.get(1);
        assertEquals(BadgeType.EMPATHETIC_DOCTOR, empathyBadge.getBadgeType());
        assertEquals("Médico Empático", empathyBadge.getBadgeName());
        assertTrue(empathyBadge.getEarned());

        BadgeProgressSummaryDTO alwaysAvailableBadge = result.get(11);
        assertEquals(BadgeType.ALWAYS_AVAILABLE, alwaysAvailableBadge.getBadgeType());
        assertEquals("Siempre Disponible", alwaysAvailableBadge.getBadgeName());
        assertEquals(0.0, alwaysAvailableBadge.getProgressPercentage());
        assertFalse(alwaysAvailableBadge.getEarned());
        assertEquals("Comienza a trabajar en esta insignia.", alwaysAvailableBadge.getStatusMessage());

        verify(userRepository).findById(doctorId);
        verify(statisticsRepository).findByDoctorId(doctorId);
        verify(badgeRepository).findByDoctor_IdAndIsActiveTrue(doctorId);
    }

    @Test
    void getBadgeProgress_DoctorNotFound_ThrowsException() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> badgeProgressService.getBadgeProgress(doctorId));

        assertEquals("Doctor not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verifyNoInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void getBadgeProgress_UserNotDoctor_ThrowsException() {
        User patient = new User();
        patient.setId(doctorId);
        patient.setRole("PATIENT");

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(patient));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> badgeProgressService.getBadgeProgress(doctorId));

        assertEquals("User is not a doctor", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verifyNoInteractions(statisticsRepository);
        verifyNoInteractions(badgeRepository);
    }

    @Test
    void getBadgeProgress_NoStatsFound_UsesEmptyStats() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.empty());
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId)).thenReturn(Collections.emptyList());

        List<BadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(doctorId);

        assertEquals(12, result.size());

        result.forEach(badge -> {
            assertEquals(0.0, badge.getProgressPercentage());
            assertFalse(badge.getEarned());
            assertEquals("Comienza a trabajar en esta insignia.", badge.getStatusMessage());
        });

        verify(userRepository).findById(doctorId);
        verify(statisticsRepository).findByDoctorId(doctorId);
        verify(badgeRepository).findByDoctor_IdAndIsActiveTrue(doctorId);
    }

    @Test
    void getBadgeProgress_NoEarnedBadges_AllBadgesNotEarned() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId)).thenReturn(Collections.emptyList());

        List<BadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(doctorId);

        assertEquals(12, result.size());

        result.forEach(badge -> {
            assertNotNull(badge.getProgressPercentage());
            assertFalse(badge.getEarned());
        });

        verify(userRepository).findById(doctorId);
        verify(statisticsRepository).findByDoctorId(doctorId);
        verify(badgeRepository).findByDoctor_IdAndIsActiveTrue(doctorId);
    }

    @Test
    void getBadgeProgress_AllBadgesEarned_AllMarkedAsEarned() {
        List<DoctorBadge> allEarnedBadges = Arrays.stream(BadgeType.values())
                .map(type -> {
                    DoctorBadge badge = new DoctorBadge();
                    badge.setBadgeType(type);
                    badge.setIsActive(true);
                    return badge;
                })
                .toList();

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId)).thenReturn(allEarnedBadges);

        List<BadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(doctorId);

        assertEquals(12, result.size());

        result.forEach(badge -> {
            assertTrue(badge.getEarned());
            assertEquals("¡Insignia obtenida! Excelente trabajo.", badge.getStatusMessage());
        });

        verify(userRepository).findById(doctorId);
        verify(statisticsRepository).findByDoctorId(doctorId);
        verify(badgeRepository).findByDoctor_IdAndIsActiveTrue(doctorId);
    }

    @Test
    void getBadgeProgress_PartialProgress_StatusMessagesCorrect() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(statisticsRepository.findByDoctorId(doctorId)).thenReturn(Optional.of(stats));
        when(badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId)).thenReturn(Collections.emptyList());

        List<BadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(doctorId);

        BadgeProgressSummaryDTO excellenceBadge = result.stream()
                .filter(b -> b.getBadgeType() == BadgeType.SUSTAINED_EXCELLENCE)
                .findFirst().orElseThrow();
        BadgeProgressSummaryDTO empathyBadge = result.stream()
                .filter(b -> b.getBadgeType() == BadgeType.EMPATHETIC_DOCTOR)
                .findFirst().orElseThrow();
        BadgeProgressSummaryDTO communicatorBadge = result.stream()
                .filter(b -> b.getBadgeType() == BadgeType.EXCEPTIONAL_COMMUNICATOR)
                .findFirst().orElseThrow();

        assertEquals(30.0, excellenceBadge.getProgressPercentage());
        assertEquals("¡Vas bien! Continúa esforzándote.", excellenceBadge.getStatusMessage());

        assertEquals(55.0, empathyBadge.getProgressPercentage());
        assertEquals("¡Buen progreso! Estás a mitad de camino.", empathyBadge.getStatusMessage());

        assertEquals(80.0, communicatorBadge.getProgressPercentage());
        assertEquals("¡Casi lo logras! Sigue así.", communicatorBadge.getStatusMessage());

        verify(userRepository).findById(doctorId);
        verify(statisticsRepository).findByDoctorId(doctorId);
        verify(badgeRepository).findByDoctor_IdAndIsActiveTrue(doctorId);
    }
}