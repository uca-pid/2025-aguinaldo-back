package com.medibook.api.service;
import com.medibook.api.dto.Badge.PatientBadgeDTO;
import com.medibook.api.dto.Badge.PatientBadgesResponseDTO;
import com.medibook.api.entity.*;
import com.medibook.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientBadgeService {


    private static final int PREVENTIVE_PATIENT_MIN_TURNS_12_MONTHS = 2;
    private static final int CONSTANT_USER_MIN_TURNS_TOTAL = 20;
    private static final double CONSTANT_USER_ATTENDANCE_RATE = 0.85;
    private static final int THERAPEUTIC_CONTINUITY_MIN_TURNS_SAME_DOCTOR = 2;
    private static final int THERAPEUTIC_CONTINUITY_MIN_SPECIALTIES = 2;

    private static final int ALWAYS_PUNCTUAL_MIN_POSITIVE_RATINGS = 9;
    private static final int ALWAYS_PUNCTUAL_MIN_TURNS = 10;
    private static final double EXPERT_PLANNER_ADVANCE_RATE = 0.8;
    private static final int EXPERT_PLANNER_MIN_ADVANCE_DAYS = 7;
    private static final double MODEL_COLLABORATOR_COLLABORATION_RATE = 0.7;
    private static final double MODEL_COLLABORATOR_FOLLOW_RATE = 0.7;
    private static final int MODEL_COLLABORATOR_MIN_TURNS = 10;

    private static final double PREPARED_PATIENT_UPLOAD_RATE = 0.8;
    private static final int CONSTRUCTIVE_EVALUATOR_MIN_RATINGS = 10;
    private static final int CONSTRUCTIVE_EVALUATOR_MIN_SUBCATEGORIES = 2;
    private static final double CONSTRUCTIVE_EVALUATOR_MIN_AVG_RATING = 3.0;
    private static final double CONSTRUCTIVE_EVALUATOR_MAX_AVG_RATING = 5.0;
    private static final int EXEMPLARY_PATIENT_MIN_OTHER_BADGES = 6;
    private static final int EXEMPLARY_PATIENT_MIN_TURNS = 50;
    private static final double EXEMPLARY_PATIENT_MIN_AVG_RATING_RECEIVED = 4.0;

    private final PatientBadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final PatientBadgeStatisticsRepository statisticsRepository;
    private final RatingRepository ratingRepository;
    private final TurnAssignedRepository turnAssignedRepository;

    @Transactional(readOnly = true)
    public PatientBadgesResponseDTO getPatientBadges(UUID patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!"PATIENT".equals(patient.getRole())) {
            throw new RuntimeException("User is not a patient");
        }

        List<PatientBadge> allBadges = badgeRepository.findByPatient_IdOrderByEarnedAtDesc(patientId);

        Map<PatientBadgeType.PatientBadgeCategory, List<PatientBadgeDTO>> badgesByCategory = allBadges.stream()
                .map(this::toBadgeDTO)
                .collect(Collectors.groupingBy(
                    badge -> badge.getBadgeType().getCategory()
                ));

        long activeBadges = allBadges.stream().filter(PatientBadge::getIsActive).count();

        return PatientBadgesResponseDTO.builder()
                .patientId(patientId)
                .patientName(patient.getName() + " " + patient.getSurname())
                .totalActiveBadges((int) activeBadges)
                .healthCommitmentBadges(badgesByCategory.getOrDefault(PatientBadgeType.PatientBadgeCategory.HEALTH_COMMITMENT, new ArrayList<>()))
                .responsibilityBadges(badgesByCategory.getOrDefault(PatientBadgeType.PatientBadgeCategory.RESPONSIBILITY, new ArrayList<>()))
                .preparationBadges(badgesByCategory.getOrDefault(PatientBadgeType.PatientBadgeCategory.PREPARATION, new ArrayList<>()))
                .build();
    }

    @Transactional
    public void evaluateAllBadges(UUID patientId) {
        log.info("Evaluating all badges for patient: {}", patientId);

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!"PATIENT".equals(patient.getRole())) {
            throw new RuntimeException("User is not a patient");
        }

        evaluatePreventivePatient(patient);
        evaluateTotalCommitment(patient);
        evaluateTherapeuticContinuity(patient);
        evaluateConstantUser(patient);
        evaluateAlwaysPunctual(patient);
        evaluateExpertPlanner(patient);
        evaluateModelCollaborator(patient);
        evaluatePreparedPatient(patient);
        evaluateConstructiveEvaluator(patient);
        evaluateExemplaryPatient(patient);

        log.info("Badge evaluation completed for patient: {}", patientId);
    }

    private void evaluatePreventivePatient(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTurnsLast12Months() < PREVENTIVE_PATIENT_MIN_TURNS_12_MONTHS) {
            deactivateBadge(patient, PatientBadgeType.PREVENTIVE_PATIENT);
            return;
        }

        List<TurnAssigned> lastYearTurns = turnAssignedRepository
                .findByPatient_IdAndStatusOrderByScheduledAtDesc(patient.getId(), "COMPLETED")
                .stream()
                .filter(t -> t.getScheduledAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC).minusYears(1)))
                .sorted((a, b) -> b.getScheduledAt().compareTo(a.getScheduledAt()))
                .toList();

        if (lastYearTurns.size() < PREVENTIVE_PATIENT_MIN_TURNS_12_MONTHS) {
            deactivateBadge(patient, PatientBadgeType.PREVENTIVE_PATIENT);
            return;
        }

        boolean hasRegularAttendance = checkRegularAttendance(lastYearTurns);

        if (hasRegularAttendance) {
            activateBadge(patient, PatientBadgeType.PREVENTIVE_PATIENT);
        } else {
            deactivateBadge(patient, PatientBadgeType.PREVENTIVE_PATIENT);
        }
    }

    private void evaluateTotalCommitment(User patient) {
        List<TurnAssigned> last5Turns = turnAssignedRepository
                .findByPatient_IdAndStatusOrderByScheduledAtDesc(patient.getId(), "COMPLETED")
                .stream().limit(5).toList();

        if (last5Turns.size() < 5) {
            deactivateBadge(patient, PatientBadgeType.TOTAL_COMMITMENT);
            return;
        }

        boolean perfectAttendance = last5Turns.stream()
                .allMatch(turn -> "COMPLETED".equals(turn.getStatus()));

        boolean noLastMinuteCancellations = checkNoLastMinuteCancellations(last5Turns);

        if (perfectAttendance && noLastMinuteCancellations) {
            activateBadge(patient, PatientBadgeType.TOTAL_COMMITMENT);
        } else {
            deactivateBadge(patient, PatientBadgeType.TOTAL_COMMITMENT);
        }
    }

    private void evaluateTherapeuticContinuity(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTurnsLast12Months() < THERAPEUTIC_CONTINUITY_MIN_TURNS_SAME_DOCTOR) {
            deactivateBadge(patient, PatientBadgeType.THERAPEUTIC_CONTINUITY);
            return;
        }

        boolean hasTurnsWithSameDoctor = stats.getTurnsWithSameDoctorLast12Months() >= THERAPEUTIC_CONTINUITY_MIN_TURNS_SAME_DOCTOR;

        boolean hasMultipleSpecialties = stats.getDifferentSpecialtiesLast12Months() >= THERAPEUTIC_CONTINUITY_MIN_SPECIALTIES;

        if (hasTurnsWithSameDoctor && hasMultipleSpecialties) {
            activateBadge(patient, PatientBadgeType.THERAPEUTIC_CONTINUITY);
        } else {
            deactivateBadge(patient, PatientBadgeType.THERAPEUTIC_CONTINUITY);
        }
    }

    private void evaluateConstantUser(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < CONSTANT_USER_MIN_TURNS_TOTAL) {
            deactivateBadge(patient, PatientBadgeType.CONSTANT_USER);
            return;
        }

        boolean activeRecently = stats.getTurnsLast6Months() > 0;

        int totalTurns = stats.getTotalTurnsCompleted() + stats.getTotalTurnsCancelled() + stats.getTotalTurnsNoShow();
        double attendanceRate = totalTurns > 0 ? (double) stats.getTotalTurnsCompleted() / totalTurns : 0.0;

        boolean goodAttendanceRate = attendanceRate >= CONSTANT_USER_ATTENDANCE_RATE;

        if (activeRecently && goodAttendanceRate) {
            activateBadge(patient, PatientBadgeType.CONSTANT_USER);
        } else {
            deactivateBadge(patient, PatientBadgeType.CONSTANT_USER);
        }
    }

    private void evaluateAlwaysPunctual(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < ALWAYS_PUNCTUAL_MIN_TURNS) {
            deactivateBadge(patient, PatientBadgeType.ALWAYS_PUNCTUAL);
            return;
        }

        boolean hasRequiredPunctualRatings = stats.getLast10TurnsPunctualCount() >= ALWAYS_PUNCTUAL_MIN_POSITIVE_RATINGS;

        if (hasRequiredPunctualRatings) {
            activateBadge(patient, PatientBadgeType.ALWAYS_PUNCTUAL);
        } else {
            deactivateBadge(patient, PatientBadgeType.ALWAYS_PUNCTUAL);
        }
    }

    private void evaluateExpertPlanner(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < 5) {
            deactivateBadge(patient, PatientBadgeType.EXPERT_PLANNER);
            return;
        }

        double advanceBookingRate = stats.getTotalTurnsCompleted() > 0 ?
                (double) stats.getLast5TurnsAdvanceBookingCount() / Math.min(stats.getTotalTurnsCompleted(), 5) : 0.0;

        boolean meetsAdvanceBookingRequirement = advanceBookingRate >= EXPERT_PLANNER_ADVANCE_RATE;

        if (meetsAdvanceBookingRequirement) {
            activateBadge(patient, PatientBadgeType.EXPERT_PLANNER);
        } else {
            deactivateBadge(patient, PatientBadgeType.EXPERT_PLANNER);
        }
    }

    private void evaluateModelCollaborator(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < MODEL_COLLABORATOR_MIN_TURNS) {
            deactivateBadge(patient, PatientBadgeType.MODEL_COLLABORATOR);
            return;
        }

        int totalRatedTurns = Math.min(stats.getTotalTurnsCompleted(), 15);
        double collaborationRate = totalRatedTurns > 0 ? (double) stats.getLast15TurnsCollaborationCount() / totalRatedTurns : 0.0;
        double followInstructionsRate = totalRatedTurns > 0 ? (double) stats.getLast15TurnsFollowInstructionsCount() / totalRatedTurns : 0.0;

        boolean meetsCollaborationRequirement = collaborationRate >= MODEL_COLLABORATOR_COLLABORATION_RATE;
        boolean meetsFollowInstructionsRequirement = followInstructionsRate >= MODEL_COLLABORATOR_FOLLOW_RATE;

        if (meetsCollaborationRequirement && meetsFollowInstructionsRequirement) {
            activateBadge(patient, PatientBadgeType.MODEL_COLLABORATOR);
        } else {
            deactivateBadge(patient, PatientBadgeType.MODEL_COLLABORATOR);
        }
    }

    private void evaluatePreparedPatient(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < 10) {
            deactivateBadge(patient, PatientBadgeType.PREPARED_PATIENT);
            return;
        }

        int last10Turns = Math.min(stats.getTotalTurnsCompleted(), 10);
        double uploadRate = last10Turns > 0 ? (double) stats.getLast10TurnsFilesUploadedCount() / last10Turns : 0.0;

        boolean meetsUploadRequirement = uploadRate >= PREPARED_PATIENT_UPLOAD_RATE;

        if (meetsUploadRequirement) {
            activateBadge(patient, PatientBadgeType.PREPARED_PATIENT);
        } else {
            deactivateBadge(patient, PatientBadgeType.PREPARED_PATIENT);
        }
    }

    private void evaluateConstructiveEvaluator(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalRatingsGiven() < CONSTRUCTIVE_EVALUATOR_MIN_RATINGS) {
            deactivateBadge(patient, PatientBadgeType.CONSTRUCTIVE_EVALUATOR);
            return;
        }

        Double avgRating = stats.getAvgRatingGiven();
        boolean hasConstructiveRatingRange = avgRating != null &&
                avgRating >= CONSTRUCTIVE_EVALUATOR_MIN_AVG_RATING &&
                avgRating <= CONSTRUCTIVE_EVALUATOR_MAX_AVG_RATING;


        if (hasConstructiveRatingRange) {
            activateBadge(patient, PatientBadgeType.CONSTRUCTIVE_EVALUATOR);
        } else {
            deactivateBadge(patient, PatientBadgeType.CONSTRUCTIVE_EVALUATOR);
        }
    }

    private void evaluateExemplaryPatient(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < EXEMPLARY_PATIENT_MIN_TURNS) {
            deactivateBadge(patient, PatientBadgeType.EXEMPLARY_PATIENT);
            return;
        }

        Double avgRatingReceived = stats.getAvgRatingReceived();
        boolean hasGoodRatingReceived = avgRatingReceived != null &&
                avgRatingReceived >= EXEMPLARY_PATIENT_MIN_AVG_RATING_RECEIVED;

        boolean activeRecently = stats.getTurnsLast90Days() > 0;

        long otherActiveBadges = badgeRepository.countActiveBadgesByPatientIdExcludingType(
                patient.getId(), PatientBadgeType.EXEMPLARY_PATIENT);

        boolean hasRequiredOtherBadges = otherActiveBadges >= EXEMPLARY_PATIENT_MIN_OTHER_BADGES;

        if (hasGoodRatingReceived && activeRecently && hasRequiredOtherBadges) {
            activateBadge(patient, PatientBadgeType.EXEMPLARY_PATIENT);
        } else {
            deactivateBadge(patient, PatientBadgeType.EXEMPLARY_PATIENT);
        }
    }

    private boolean checkRegularAttendance(List<TurnAssigned> turns) {
        if (turns.size() < PREVENTIVE_PATIENT_MIN_TURNS_12_MONTHS) return false;

        turns.sort((a, b) -> a.getScheduledAt().compareTo(b.getScheduledAt()));

        OffsetDateTime firstTurn = turns.get(0).getScheduledAt();
        OffsetDateTime lastTurn = turns.get(turns.size() - 1).getScheduledAt();

        long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(firstTurn, lastTurn);
        int requiredPeriods = (int) Math.ceil(monthsBetween / 6.0);

        return turns.size() >= requiredPeriods;
    }

    private boolean checkNoLastMinuteCancellations(List<TurnAssigned> turns) {
        return true;
    }

    private PatientBadgeStatistics getOrCreateStatistics(UUID patientId) {
        return statisticsRepository.findByPatientId(patientId)
                .orElseGet(() -> {
                    User patient = userRepository.findById(patientId)
                            .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

                    PatientBadgeStatistics stats = PatientBadgeStatistics.builder()
                            .patient(patient)
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
                            .totalUniqueDoctors(0)
                            .turnsWithSameDoctorLast12Months(0)
                            .differentSpecialtiesLast12Months(0)
                            .build();

                    return statisticsRepository.save(stats);
                });
    }

    private void activateBadge(User patient, PatientBadgeType badgeType) {
        Optional<PatientBadge> existing = badgeRepository.findByPatient_IdAndBadgeType(
                patient.getId(), badgeType);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (existing.isPresent()) {
            PatientBadge badge = existing.get();
            if (!badge.getIsActive()) {
                badge.setIsActive(true);
                badge.setLastEvaluatedAt(now);
                badgeRepository.save(badge);
                log.info("Reactivated badge {} for patient {}", badgeType, patient.getId());
            } else {
                badge.setLastEvaluatedAt(now);
                badgeRepository.save(badge);
            }
        } else {
            PatientBadge newBadge = PatientBadge.builder()
                    .patient(patient)
                    .badgeType(badgeType)
                    .earnedAt(now)
                    .isActive(true)
                    .lastEvaluatedAt(now)
                    .build();
            badgeRepository.save(newBadge);
            log.info("Awarded new badge {} to patient {}", badgeType, patient.getId());
        }
    }

    private void deactivateBadge(User patient, PatientBadgeType badgeType) {
        Optional<PatientBadge> existing = badgeRepository.findByPatient_IdAndBadgeType(
                patient.getId(), badgeType);

        if (existing.isPresent() && existing.get().getIsActive()) {
            PatientBadge badge = existing.get();
            badge.setIsActive(false);
            badge.setLastEvaluatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            badgeRepository.save(badge);
            log.info("Deactivated badge {} for patient {}", badgeType, patient.getId());
        } else if (existing.isPresent()) {
            PatientBadge badge = existing.get();
            badge.setLastEvaluatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            badgeRepository.save(badge);
        }
    }

    private PatientBadgeDTO toBadgeDTO(PatientBadge badge) {
        return PatientBadgeDTO.builder()
                .badgeType(badge.getBadgeType())
                .category(badge.getBadgeType().getCategory().name())
                .isActive(badge.getIsActive())
                .earnedAt(badge.getEarnedAt())
                .lastEvaluatedAt(badge.getLastEvaluatedAt())
                .build();
    }
}