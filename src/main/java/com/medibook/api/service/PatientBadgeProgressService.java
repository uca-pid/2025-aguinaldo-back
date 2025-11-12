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
        log.info("Fetching badge progress for patient: {}", patientId);

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
                PatientBadgeType.PREVENTIVE_PATIENT,
                "Paciente Preventivo",
                PatientBadgeCategory.HEALTH_COMMITMENT,
                stats.getProgressPreventivePatient(),
                earnedBadges.contains(PatientBadgeType.PREVENTIVE_PATIENT),
                "Completa 2+ turnos en los últimos 12 meses"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.TOTAL_COMMITMENT,
                "Guardián de la Salud",
                PatientBadgeCategory.HEALTH_COMMITMENT,
                stats.getProgressTotalCommitment(),
                earnedBadges.contains(PatientBadgeType.TOTAL_COMMITMENT),
                "Completa 25+ turnos en total"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.THERAPEUTIC_CONTINUITY,
                "Campeón del Bienestar",
                PatientBadgeCategory.HEALTH_COMMITMENT,
                stats.getProgressTherapeuticContinuity(),
                earnedBadges.contains(PatientBadgeType.THERAPEUTIC_CONTINUITY),
                "Completa 50+ turnos en total"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.CONSTANT_USER,
                "Compromiso Total",
                PatientBadgeCategory.HEALTH_COMMITMENT,
                stats.getProgressConstantUser(),
                earnedBadges.contains(PatientBadgeType.CONSTANT_USER),
                "Completa 100+ turnos en total"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.ALWAYS_PUNCTUAL,
                "Asistente Consistente",
                PatientBadgeCategory.RESPONSIBILITY,
                stats.getProgressAlwaysPunctual(),
                earnedBadges.contains(PatientBadgeType.ALWAYS_PUNCTUAL),
                "Completa 10+ turnos sin cancelaciones"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.EXPERT_PLANNER,
                "Paciente Confiable",
                PatientBadgeCategory.RESPONSIBILITY,
                stats.getProgressExpertPlanner(),
                earnedBadges.contains(PatientBadgeType.EXPERT_PLANNER),
                "Completa 20+ turnos sin no-shows"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.MODEL_COLLABORATOR,
                "Estrella Puntual",
                PatientBadgeCategory.RESPONSIBILITY,
                stats.getProgressModelCollaborator(),
                earnedBadges.contains(PatientBadgeType.MODEL_COLLABORATOR),
                "Completa 15+ turnos sin no-shows"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.PREPARED_PATIENT,
                "Paciente Preparado",
                PatientBadgeCategory.PREPARATION,
                stats.getProgressPreparedPatient(),
                earnedBadges.contains(PatientBadgeType.PREPARED_PATIENT),
                "Sube 8+ archivos en los últimos 10 turnos"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.CONSTRUCTIVE_EVALUATOR,
                "Planificador Anticipado",
                PatientBadgeCategory.PREPARATION,
                stats.getProgressConstructiveEvaluator(),
                earnedBadges.contains(PatientBadgeType.CONSTRUCTIVE_EVALUATOR),
                "Reserva 4+ turnos con anticipación en los últimos 5"
        ));

        progressList.add(createProgressDTO(
                PatientBadgeType.EXEMPLARY_PATIENT,
                "Preparador Excelente",
                PatientBadgeCategory.PREPARATION,
                stats.getProgressExemplaryPatient(),
                earnedBadges.contains(PatientBadgeType.EXEMPLARY_PATIENT),
                "Sube 10+ archivos Y reserva 4+ turnos con anticipación"
        ));

        log.info("Badge progress fetched successfully for patient: {}", patientId);
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
                .progressPreventivePatient(0.0)
                .progressTotalCommitment(0.0)
                .progressTherapeuticContinuity(0.0)
                .progressConstantUser(0.0)
                .progressAlwaysPunctual(0.0)
                .progressExpertPlanner(0.0)
                .progressModelCollaborator(0.0)
                .progressPreparedPatient(0.0)
                .progressConstructiveEvaluator(0.0)
                .progressExemplaryPatient(0.0)
                .build();
    }
}