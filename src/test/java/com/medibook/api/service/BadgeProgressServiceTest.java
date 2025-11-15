package com.medibook.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.medibook.api.dto.Badge.BadgeProgressSummaryDTO;
import com.medibook.api.entity.*;
import com.medibook.api.entity.BadgeType.BadgeCategory;
import com.medibook.api.model.BadgeMetadata;
import com.medibook.api.repository.BadgeRepository;
import com.medibook.api.repository.BadgeStatisticsRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeProgressServiceTest {

    @Mock
    private BadgeStatisticsRepository statisticsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private BadgeMetadataService badgeMetadataService;

    @InjectMocks
    private BadgeProgressService badgeProgressService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private UUID userId;
    private User doctor;
    private User patient;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        doctor = new User();
        doctor.setId(userId);
        doctor.setName("Dr. Test");
        doctor.setSurname("Doctor");
        doctor.setRole("DOCTOR");
        doctor.setStatus("ACTIVE");

        patient = new User();
        patient.setId(UUID.randomUUID());
        patient.setName("Test");
        patient.setSurname("Patient");
        patient.setRole("PATIENT");
        patient.setStatus("ACTIVE");
    }

    @Test
    void getBadgeProgress_Doctor_ReturnsDoctorProgress() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(doctor));
        when(badgeMetadataService.getAllDoctorBadgeMetadata()).thenReturn(createDoctorMetadata());

        BadgeStatistics stats = BadgeStatistics.builder()
                .userId(userId)
                .progress(createDoctorProgressJson())
                .build();
        when(statisticsRepository.findByUserId(userId)).thenReturn(Optional.of(stats));

        List<Badge> earnedBadges = List.of(
                Badge.builder().badgeType("DOCTOR_SUSTAINED_EXCELLENCE").isActive(true).build()
        );
        when(badgeRepository.findByUser_IdOrderByEarnedAtDesc(userId)).thenReturn(earnedBadges);

        List<BadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(dto -> "DOCTOR_SUSTAINED_EXCELLENCE".equals(dto.getBadgeType())));
        assertTrue(result.stream().anyMatch(dto -> dto.getEarned()));

        verify(userRepository).findById(userId);
        verify(statisticsRepository).findByUserId(userId);
        verify(badgeRepository).findByUser_IdOrderByEarnedAtDesc(userId);
    }

    @Test
    void getBadgeProgress_Patient_ReturnsPatientProgress() {
        UUID patientId = patient.getId();
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(badgeMetadataService.getAllPatientBadgeMetadata()).thenReturn(createPatientMetadata());

        BadgeStatistics stats = BadgeStatistics.builder()
                .userId(patientId)
                .progress(createPatientProgressJson())
                .build();
        when(statisticsRepository.findByUserId(patientId)).thenReturn(Optional.of(stats));

        List<Badge> earnedBadges = List.of(
                Badge.builder().badgeType("PATIENT_MEDIBOOK_WELCOME").isActive(true).build()
        );
        when(badgeRepository.findByUser_IdOrderByEarnedAtDesc(patientId)).thenReturn(earnedBadges);

        List<BadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(patientId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(dto -> "PATIENT_MEDIBOOK_WELCOME".equals(dto.getBadgeType())));
        assertTrue(result.stream().anyMatch(dto -> dto.getEarned()));

        verify(userRepository).findById(patientId);
        verify(statisticsRepository).findByUserId(patientId);
        verify(badgeRepository).findByUser_IdOrderByEarnedAtDesc(patientId);
    }

    @Test
    void getPatientBadgeProgress_Patient_ReturnsPatientProgress() {
        UUID patientId = patient.getId();
        when(badgeMetadataService.getAllPatientBadgeMetadata()).thenReturn(createPatientMetadata());

        BadgeStatistics stats = BadgeStatistics.builder()
                .userId(patientId)
                .progress(createPatientProgressJson())
                .build();
        when(statisticsRepository.findByUserId(patientId)).thenReturn(Optional.of(stats));

        List<Badge> earnedBadges = List.of(
                Badge.builder().badgeType("PATIENT_MEDIBOOK_WELCOME").isActive(true).build()
        );
        when(badgeRepository.findByUser_IdOrderByEarnedAtDesc(patientId)).thenReturn(earnedBadges);

        List<BadgeProgressSummaryDTO> result = badgeProgressService.getPatientBadgeProgress(patient);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(dto -> "PATIENT_MEDIBOOK_WELCOME".equals(dto.getBadgeType())));
        assertTrue(result.stream().anyMatch(dto -> dto.getEarned()));

        verify(statisticsRepository).findByUserId(patientId);
        verify(badgeRepository).findByUser_IdOrderByEarnedAtDesc(patientId);
    }

    @Test
    void getPatientBadgeProgress_UserNotPatient_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> badgeProgressService.getPatientBadgeProgress(doctor));

        assertEquals("User is not a patient", exception.getMessage());
    }

    @Test
    void getBadgeProgress_NoStatistics_ReturnsEmptyProgress() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(doctor));
        when(badgeMetadataService.getAllDoctorBadgeMetadata()).thenReturn(createDoctorMetadata());
        when(statisticsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        List<BadgeProgressSummaryDTO> result = badgeProgressService.getBadgeProgress(userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(dto -> dto.getProgressPercentage() == 0.0));
        assertTrue(result.stream().noneMatch(dto -> dto.getEarned()));
    }

    @Test
    void getPatientBadgeProgress_NoStatistics_ReturnsEmptyProgress() {
        UUID patientId = patient.getId();
        when(badgeMetadataService.getAllPatientBadgeMetadata()).thenReturn(createPatientMetadata());
        when(statisticsRepository.findByUserId(patientId)).thenReturn(Optional.empty());

        List<BadgeProgressSummaryDTO> result = badgeProgressService.getPatientBadgeProgress(patient);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(dto -> dto.getProgressPercentage() == 0.0));
        assertTrue(result.stream().noneMatch(dto -> dto.getEarned()));
    }

    private ObjectNode createDoctorProgressJson() {
        ObjectNode progress = objectMapper.createObjectNode();
        progress.put("DOCTOR_SUSTAINED_EXCELLENCE", 100.0);
        progress.put("DOCTOR_EMPATHETIC_DOCTOR", 75.0);
        progress.put("DOCTOR_EXCEPTIONAL_COMMUNICATOR", 50.0);
        return progress;
    }

    private ObjectNode createPatientProgressJson() {
        ObjectNode progress = objectMapper.createObjectNode();
        progress.put("PATIENT_MEDIBOOK_WELCOME", 100.0);
        progress.put("PATIENT_HEALTH_GUARDIAN", 60.0);
        progress.put("PATIENT_COMMITTED_PATIENT", 40.0);
        return progress;
    }

    private Map<String, BadgeMetadata> createDoctorMetadata() {
        Map<String, BadgeMetadata> metadata = new java.util.HashMap<>();
        metadata.put("DOCTOR_SUSTAINED_EXCELLENCE", BadgeMetadata.builder()
                .name("Excelencia Sostenida")
                .category(BadgeCategory.QUALITY_OF_CARE)
                .rarity(BadgeMetadata.BadgeRarity.EPIC)
                .description("Mantiene consistentemente altas calificaciones")
                .icon("⭐")
                .color("#FF9800")
                .criteria("Mantén un promedio de 4.7 en 100+ turnos")
                .build());
        metadata.put("DOCTOR_EMPATHETIC_DOCTOR", BadgeMetadata.builder()
                .name("Médico Empático")
                .category(BadgeCategory.QUALITY_OF_CARE)
                .rarity(BadgeMetadata.BadgeRarity.RARE)
                .description("Destaca por su empatía")
                .icon("❤️")
                .color("#E91E63")
                .criteria("Recibe 25 menciones positivas de empatía")
                .build());
        metadata.put("DOCTOR_EXCEPTIONAL_COMMUNICATOR", BadgeMetadata.builder()
                .name("Comunicador Excepcional")
                .category(BadgeCategory.QUALITY_OF_CARE)
                .rarity(BadgeMetadata.BadgeRarity.RARE)
                .description("Excelente comunicación")
                .icon("\uD83D\uDCAC")
                .color("#4CAF50")
                .criteria("Recibe 25 menciones positivas de comunicación")
                .build());
        return metadata;
    }

    private Map<String, BadgeMetadata> createPatientMetadata() {
        Map<String, BadgeMetadata> metadata = new java.util.HashMap<>();
        metadata.put("PATIENT_MEDIBOOK_WELCOME", BadgeMetadata.builder()
                .name("Bienvenido a MediBook")
                .category(BadgeCategory.WELCOME)
                .rarity(BadgeMetadata.BadgeRarity.COMMON)
                .description("Primeros pasos en MediBook")
                .icon("👋")
                .color("#78909C")
                .criteria("Completa tu primer turno")
                .build());
        metadata.put("PATIENT_HEALTH_GUARDIAN", BadgeMetadata.builder()
                .name("Guardián de la Salud")
                .category(BadgeCategory.PREVENTIVE_CARE)
                .rarity(BadgeMetadata.BadgeRarity.RARE)
                .description("Comprometido con la salud")
                .icon("🏥")
                .color("#2196F3")
                .criteria("Completa 6+ turnos")
                .build());
        metadata.put("PATIENT_COMMITTED_PATIENT", BadgeMetadata.builder()
                .name("Paciente Comprometido")
                .category(BadgeCategory.PREVENTIVE_CARE)
                .rarity(BadgeMetadata.BadgeRarity.COMMON)
                .description("Asistencia regular")
                .icon("📅")
                .color("#FF5722")
                .criteria("Completa 5+ turnos")
                .build());
        return metadata;
    }
}