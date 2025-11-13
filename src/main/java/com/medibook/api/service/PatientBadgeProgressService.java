package com.medibook.api.service;

import com.medibook.api.dto.Badge.PatientBadgeProgressSummaryDTO;
import com.medibook.api.entity.PatientBadgeStatistics;
import com.medibook.api.entity.PatientBadgeType;
import com.medibook.api.entity.PatientBadgeType.PatientBadgeCategory;
import com.medibook.api.entity.User;
import com.medibook.api.repository.PatientBadgeRepository;
import com.medibook.api.repository.PatientBadgeStatisticsRepository;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientBadgeProgressService {

    private final PatientBadgeStatisticsRepository statisticsRepository;
    private final UserRepository userRepository;
    private final PatientBadgeRepository badgeRepository;

    @Transactional(readOnly = true)
    public List<PatientBadgeProgressSummaryDTO> getBadgeProgress(UUID patientId) {

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!"PATIENT".equals(patient.getRole())) {
            throw new RuntimeException("User is not a patient");
        }


        PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId)
                .orElseGet(() -> createEmptyStatistics(patientId));


        List<PatientBadgeType> earnedBadges = badgeRepository.findByPatient_IdAndIsActiveTrue(patientId)
                .stream()
                .map(badge -> badge.getBadgeType())
                .toList();


        List<PatientBadgeProgressSummaryDTO> progressList = new ArrayList<>();

        progressList.add(createProgressDTO(
                PatientBadgeType.MEDIBOOK_WELCOME,
                "Bienvenido a MediBook",
                PatientBadgeCategory.WELCOME,
                stats.getProgressMediBookWelcome(),
                earnedBadges.contains(PatientBadgeType.MEDIBOOK_WELCOME),
                "Completa tu primer turno"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.HEALTH_GUARDIAN,
                "Guardián de la Salud",
                PatientBadgeCategory.PREVENTIVE_CARE,
                stats.getProgressHealthGuardian(),
                earnedBadges.contains(PatientBadgeType.HEALTH_GUARDIAN),
                "Completa 3+ turnos en los últimos 6 meses"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.COMMITTED_PATIENT,
                "Paciente Comprometido",
                PatientBadgeCategory.ACTIVE_COMMITMENT,
                stats.getProgressCommittedPatient(),
                earnedBadges.contains(PatientBadgeType.COMMITTED_PATIENT),
                "Completa 5 turnos consecutivos"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.CONTINUOUS_FOLLOWUP,
                "Seguimiento Continuo",
                PatientBadgeCategory.ACTIVE_COMMITMENT,
                stats.getProgressContinuousFollowup(),
                earnedBadges.contains(PatientBadgeType.CONTINUOUS_FOLLOWUP),
                "Completa 3+ turnos con el mismo doctor"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.CONSTANT_PATIENT,
                "Paciente Constante",
                PatientBadgeCategory.ACTIVE_COMMITMENT,
                stats.getProgressConstantPatient(),
                earnedBadges.contains(PatientBadgeType.CONSTANT_PATIENT),
                "Completa 15+ turnos en 12 meses con 75% asistencia"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.EXEMPLARY_PUNCTUALITY,
                "Puntualidad Ejemplar",
                PatientBadgeCategory.CLINICAL_EXCELLENCE,
                stats.getProgressExemplaryPunctuality(),
                earnedBadges.contains(PatientBadgeType.EXEMPLARY_PUNCTUALITY),
                "Obtén 8+ calificaciones positivas de puntualidad"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.SMART_PLANNER,
                "Planificador Inteligente",
                PatientBadgeCategory.CLINICAL_EXCELLENCE,
                stats.getProgressSmartPlanner(),
                earnedBadges.contains(PatientBadgeType.SMART_PLANNER),
                "Reserva 70% de turnos con anticipación"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.EXCELLENT_COLLABORATOR,
                "Colaborador Excelente",
                PatientBadgeCategory.CLINICAL_EXCELLENCE,
                stats.getProgressExcellentCollaborator(),
                earnedBadges.contains(PatientBadgeType.EXCELLENT_COLLABORATOR),
                "Obtén buenas calificaciones de colaboración"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.ALWAYS_PREPARED,
                "Siempre Preparado",
                PatientBadgeCategory.CLINICAL_EXCELLENCE,
                stats.getProgressAlwaysPrepared(),
                earnedBadges.contains(PatientBadgeType.ALWAYS_PREPARED),
                "Sube 70% de documentos requeridos"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.RESPONSIBLE_EVALUATOR,
                "Evaluador Responsable",
                PatientBadgeCategory.CLINICAL_EXCELLENCE,
                stats.getProgressResponsibleEvaluator(),
                earnedBadges.contains(PatientBadgeType.RESPONSIBLE_EVALUATOR),
                "Proporciona evaluaciones constructivas"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.EXCELLENCE_MODEL,
                "Modelo de Excelencia",
                PatientBadgeCategory.CLINICAL_EXCELLENCE,
                stats.getProgressExcellenceModel(),
                earnedBadges.contains(PatientBadgeType.EXCELLENCE_MODEL),
                "Completa 25+ turnos con 4+ otros badges y 4.0+ rating"
        ));

        return progressList;
    }

    private PatientBadgeProgressSummaryDTO createProgressDTO(
            PatientBadgeType type,
            String name,
            PatientBadgeCategory category,
            Double progress,
            Boolean earned,
            String description
    ) {
        String statusMessage = earned ?
                "¡Insignia obtenida! Excelente trabajo." :
                (progress >= 75 ? "¡Casi lo logras! Sigue así." :
                        progress >= 50 ? "¡Buen progreso! Estás a mitad de camino." :
                                progress >= 25 ? "¡Vas bien! Continúa esforzándote." :
                                        "Comienza a trabajar en esta insignia.");

        return PatientBadgeProgressSummaryDTO.builder()
                .badgeType(type)
                .badgeName(name)
                .category(category)
                .earned(earned)
                .progressPercentage(progress)
                .description(description)
                .statusMessage(statusMessage)
                .build();
    }

    private PatientBadgeStatistics createEmptyStatistics(UUID patientId) {
        return PatientBadgeStatistics.builder()
                .patientId(patientId)
                .totalTurnsCompleted(0)
                .totalTurnsCancelled(0)
                .totalTurnsNoShow(0)
                .turnsLast12Months(0)
                .turnsLast6Months(0)
                .turnsLast90Days(0)
                .last5TurnsAttendanceRate(0.0)
                .last10TurnsPunctualCount(0)
                .last5TurnsAdvanceBookingCount(0)
                .last15TurnsCollaborationCount(0)
                .last15TurnsFollowInstructionsCount(0)
                .last10TurnsFilesUploadedCount(0)
                .totalRatingsGiven(0)
                .totalRatingsReceived(0)
                .avgRatingGiven(0.0)
                .avgRatingReceived(0.0)
                .totalUniqueDoctors(0)
                .turnsWithSameDoctorLast12Months(0)
                .differentSpecialtiesLast12Months(0)
                .progressMediBookWelcome(0.0)
                .progressHealthGuardian(0.0)
                .progressCommittedPatient(0.0)
                .progressContinuousFollowup(0.0)
                .progressConstantPatient(0.0)
                .progressExemplaryPunctuality(0.0)
                .progressSmartPlanner(0.0)
                .progressExcellentCollaborator(0.0)
                .progressAlwaysPrepared(0.0)
                .progressResponsibleEvaluator(0.0)
                .progressExcellenceModel(0.0)
                .lastUpdatedAt(OffsetDateTime.now())
                .build();
    }
}