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


    private static final int MEDIBOOK_WELCOME_MIN_TURNS = 1;

    private static final int HEALTH_GUARDIAN_MIN_TURNS_6_MONTHS = 3;
    private static final int CONSTANT_PATIENT_MIN_TURNS_TOTAL = 15;
    private static final double CONSTANT_PATIENT_ATTENDANCE_RATE = 0.75;
    private static final int CONTINUOUS_FOLLOWUP_MIN_TURNS_SAME_DOCTOR = 3;

    private static final int EXEMPLARY_PUNCTUALITY_MIN_POSITIVE_RATINGS = 8;
    private static final int EXEMPLARY_PUNCTUALITY_MIN_TURNS = 8;
    private static final double SMART_PLANNER_ADVANCE_RATE = 0.7;
    private static final int SMART_PLANNER_MIN_ADVANCE_DAYS = 3;
    private static final double EXCELLENT_COLLABORATOR_COLLABORATION_RATE = 0.7;
    private static final double EXCELLENT_COLLABORATOR_FOLLOW_RATE = 0.7;
    private static final int EXCELLENT_COLLABORATOR_MIN_TURNS = 8;

    private static final double ALWAYS_PREPARED_UPLOAD_RATE = 0.7;
    private static final int RESPONSIBLE_EVALUATOR_MIN_RATINGS = 8;
    private static final int RESPONSIBLE_EVALUATOR_MIN_SUBCATEGORIES = 2;
    private static final double RESPONSIBLE_EVALUATOR_MIN_AVG_RATING = 3.0;
    private static final double RESPONSIBLE_EVALUATOR_MAX_AVG_RATING = 5.0;
    private static final int EXCELLENCE_MODEL_MIN_OTHER_BADGES = 4;
    private static final int EXCELLENCE_MODEL_MIN_TURNS = 25;
    private static final double EXCELLENCE_MODEL_MIN_AVG_RATING_RECEIVED = 4.0;

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
                .welcomeBadges(badgesByCategory.getOrDefault(PatientBadgeType.PatientBadgeCategory.WELCOME, new ArrayList<>()))
                .preventiveCareBadges(badgesByCategory.getOrDefault(PatientBadgeType.PatientBadgeCategory.PREVENTIVE_CARE, new ArrayList<>()))
                .activeCommitmentBadges(badgesByCategory.getOrDefault(PatientBadgeType.PatientBadgeCategory.ACTIVE_COMMITMENT, new ArrayList<>()))
                .clinicalExcellenceBadges(badgesByCategory.getOrDefault(PatientBadgeType.PatientBadgeCategory.CLINICAL_EXCELLENCE, new ArrayList<>()))
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

        evaluateMediBookWelcome(patient);
        evaluateHealthGuardian(patient);
        evaluateCommittedPatient(patient);
        evaluateContinuousFollowup(patient);
        evaluateConstantPatient(patient);
        evaluateExemplaryPunctuality(patient);
        evaluateSmartPlanner(patient);
        evaluateExcellentCollaborator(patient);
        evaluateAlwaysPrepared(patient);
        evaluateResponsibleEvaluator(patient);
        evaluateExcellenceModel(patient);

        log.info("Badge evaluation completed for patient: {}", patientId);
    }

    private void evaluateMediBookWelcome(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() >= MEDIBOOK_WELCOME_MIN_TURNS) {
            activateBadge(patient, PatientBadgeType.MEDIBOOK_WELCOME);
        } else {
            deactivateBadge(patient, PatientBadgeType.MEDIBOOK_WELCOME);
        }
    }

    private void evaluateHealthGuardian(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTurnsLast6Months() < HEALTH_GUARDIAN_MIN_TURNS_6_MONTHS) {
            deactivateBadge(patient, PatientBadgeType.HEALTH_GUARDIAN);
            return;
        }

        List<TurnAssigned> last6MonthsTurns = turnAssignedRepository
                .findByPatient_IdAndStatusOrderByScheduledAtDesc(patient.getId(), "COMPLETED")
                .stream()
                .filter(t -> t.getScheduledAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC).minusMonths(6)))
                .toList();

        if (last6MonthsTurns.size() >= HEALTH_GUARDIAN_MIN_TURNS_6_MONTHS) {
            activateBadge(patient, PatientBadgeType.HEALTH_GUARDIAN);
        } else {
            deactivateBadge(patient, PatientBadgeType.HEALTH_GUARDIAN);
        }
    }

    private void evaluateCommittedPatient(User patient) {
        List<TurnAssigned> last5Turns = turnAssignedRepository
                .findByPatient_IdAndStatusOrderByScheduledAtDesc(patient.getId(), "COMPLETED")
                .stream().limit(5).toList();

        if (last5Turns.size() >= 5) {
            activateBadge(patient, PatientBadgeType.COMMITTED_PATIENT);
        } else {
            deactivateBadge(patient, PatientBadgeType.COMMITTED_PATIENT);
        }
    }

    private void evaluateContinuousFollowup(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTurnsWithSameDoctorLast12Months() >= CONTINUOUS_FOLLOWUP_MIN_TURNS_SAME_DOCTOR) {
            activateBadge(patient, PatientBadgeType.CONTINUOUS_FOLLOWUP);
        } else {
            deactivateBadge(patient, PatientBadgeType.CONTINUOUS_FOLLOWUP);
        }
    }

    private void evaluateConstantPatient(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < CONSTANT_PATIENT_MIN_TURNS_TOTAL) {
            deactivateBadge(patient, PatientBadgeType.CONSTANT_PATIENT);
            return;
        }

        boolean activeRecently = stats.getTurnsLast6Months() > 0;

        int totalTurns = stats.getTotalTurnsCompleted() + stats.getTotalTurnsCancelled() + stats.getTotalTurnsNoShow();
        double attendanceRate = totalTurns > 0 ? (double) stats.getTotalTurnsCompleted() / totalTurns : 0.0;

        boolean goodAttendanceRate = attendanceRate >= CONSTANT_PATIENT_ATTENDANCE_RATE;

        if (activeRecently && goodAttendanceRate) {
            activateBadge(patient, PatientBadgeType.CONSTANT_PATIENT);
        } else {
            deactivateBadge(patient, PatientBadgeType.CONSTANT_PATIENT);
        }
    }

    private void evaluateExemplaryPunctuality(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < EXEMPLARY_PUNCTUALITY_MIN_TURNS) {
            deactivateBadge(patient, PatientBadgeType.EXEMPLARY_PUNCTUALITY);
            return;
        }

        boolean hasRequiredPunctualRatings = stats.getLast10TurnsPunctualCount() >= EXEMPLARY_PUNCTUALITY_MIN_POSITIVE_RATINGS;

        if (hasRequiredPunctualRatings) {
            activateBadge(patient, PatientBadgeType.EXEMPLARY_PUNCTUALITY);
        } else {
            deactivateBadge(patient, PatientBadgeType.EXEMPLARY_PUNCTUALITY);
        }
    }

    private void evaluateSmartPlanner(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < 5) {
            deactivateBadge(patient, PatientBadgeType.SMART_PLANNER);
            return;
        }

        double advanceBookingRate = stats.getTotalTurnsCompleted() > 0 ?
                (double) stats.getLast5TurnsAdvanceBookingCount() / Math.min(stats.getTotalTurnsCompleted(), 5) : 0.0;

        boolean meetsAdvanceBookingRequirement = advanceBookingRate >= SMART_PLANNER_ADVANCE_RATE;

        if (meetsAdvanceBookingRequirement) {
            activateBadge(patient, PatientBadgeType.SMART_PLANNER);
        } else {
            deactivateBadge(patient, PatientBadgeType.SMART_PLANNER);
        }
    }

    private void evaluateExcellentCollaborator(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < EXCELLENT_COLLABORATOR_MIN_TURNS) {
            deactivateBadge(patient, PatientBadgeType.EXCELLENT_COLLABORATOR);
            return;
        }

        int totalRatedTurns = Math.min(stats.getTotalTurnsCompleted(), 15);
        double collaborationRate = totalRatedTurns > 0 ? (double) stats.getLast15TurnsCollaborationCount() / totalRatedTurns : 0.0;
        double followInstructionsRate = totalRatedTurns > 0 ? (double) stats.getLast15TurnsFollowInstructionsCount() / totalRatedTurns : 0.0;

        boolean meetsCollaborationRequirement = collaborationRate >= EXCELLENT_COLLABORATOR_COLLABORATION_RATE;
        boolean meetsFollowInstructionsRequirement = followInstructionsRate >= EXCELLENT_COLLABORATOR_FOLLOW_RATE;

        if (meetsCollaborationRequirement && meetsFollowInstructionsRequirement) {
            activateBadge(patient, PatientBadgeType.EXCELLENT_COLLABORATOR);
        } else {
            deactivateBadge(patient, PatientBadgeType.EXCELLENT_COLLABORATOR);
        }
    }

    private void evaluateAlwaysPrepared(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < 7) {
            deactivateBadge(patient, PatientBadgeType.ALWAYS_PREPARED);
            return;
        }

        int last7Turns = Math.min(stats.getTotalTurnsCompleted(), 7);
        double uploadRate = last7Turns > 0 ? (double) stats.getLast10TurnsFilesUploadedCount() / last7Turns : 0.0;

        boolean meetsUploadRequirement = uploadRate >= ALWAYS_PREPARED_UPLOAD_RATE;

        if (meetsUploadRequirement) {
            activateBadge(patient, PatientBadgeType.ALWAYS_PREPARED);
        } else {
            deactivateBadge(patient, PatientBadgeType.ALWAYS_PREPARED);
        }
    }

    private void evaluateResponsibleEvaluator(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalRatingsGiven() < RESPONSIBLE_EVALUATOR_MIN_RATINGS) {
            deactivateBadge(patient, PatientBadgeType.RESPONSIBLE_EVALUATOR);
            return;
        }

        Double avgRating = stats.getAvgRatingGiven();
        boolean hasConstructiveRatingRange = avgRating != null &&
                avgRating >= RESPONSIBLE_EVALUATOR_MIN_AVG_RATING &&
                avgRating <= RESPONSIBLE_EVALUATOR_MAX_AVG_RATING;


        if (hasConstructiveRatingRange) {
            activateBadge(patient, PatientBadgeType.RESPONSIBLE_EVALUATOR);
        } else {
            deactivateBadge(patient, PatientBadgeType.RESPONSIBLE_EVALUATOR);
        }
    }

    private void evaluateExcellenceModel(User patient) {
        PatientBadgeStatistics stats = getOrCreateStatistics(patient.getId());

        if (stats.getTotalTurnsCompleted() < EXCELLENCE_MODEL_MIN_TURNS) {
            deactivateBadge(patient, PatientBadgeType.EXCELLENCE_MODEL);
            return;
        }

        Double avgRatingReceived = stats.getAvgRatingReceived();
        boolean hasGoodRatingReceived = avgRatingReceived != null &&
                avgRatingReceived >= EXCELLENCE_MODEL_MIN_AVG_RATING_RECEIVED;

        boolean activeRecently = stats.getTurnsLast90Days() > 0;

        long otherActiveBadges = badgeRepository.countActiveBadgesByPatientIdExcludingType(
                patient.getId(), PatientBadgeType.EXCELLENCE_MODEL);

        boolean hasRequiredOtherBadges = otherActiveBadges >= EXCELLENCE_MODEL_MIN_OTHER_BADGES;

        if (hasGoodRatingReceived && activeRecently && hasRequiredOtherBadges) {
            activateBadge(patient, PatientBadgeType.EXCELLENCE_MODEL);
        } else {
            deactivateBadge(patient, PatientBadgeType.EXCELLENCE_MODEL);
        }
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

    @Transactional
    public void evaluateTurnRelatedBadges(UUID patientId) {
        log.info("Evaluating turn-related badges for patient: {}", patientId);

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!"PATIENT".equals(patient.getRole())) {
            throw new RuntimeException("User is not a patient");
        }

        evaluateMediBookWelcome(patient);
        evaluateHealthGuardian(patient);
        evaluateCommittedPatient(patient);
        evaluateContinuousFollowup(patient);
        evaluateConstantPatient(patient);

        log.info("Turn-related badge evaluation completed for patient: {}", patientId);
    }

    @Transactional
    public void evaluateRatingRelatedBadges(UUID patientId) {
        log.info("Evaluating rating-related badges for patient: {}", patientId);

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!"PATIENT".equals(patient.getRole())) {
            throw new RuntimeException("User is not a patient");
        }

        evaluateExemplaryPunctuality(patient);
        evaluateResponsibleEvaluator(patient);
        evaluateExcellentCollaborator(patient);
        evaluateExcellenceModel(patient);

        log.info("Rating-related badge evaluation completed for patient: {}", patientId);
    }

    @Transactional
    public void evaluateFileRelatedBadges(UUID patientId) {
        log.info("Evaluating file-related badges for patient: {}", patientId);

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!"PATIENT".equals(patient.getRole())) {
            throw new RuntimeException("User is not a patient");
        }

        evaluateAlwaysPrepared(patient);

        log.info("File-related badge evaluation completed for patient: {}", patientId);
    }

    @Transactional
    public void evaluateBookingRelatedBadges(UUID patientId) {
        log.info("Evaluating booking-related badges for patient: {}", patientId);

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!"PATIENT".equals(patient.getRole())) {
            throw new RuntimeException("User is not a patient");
        }

        evaluateSmartPlanner(patient);

        log.info("Booking-related badge evaluation completed for patient: {}", patientId);
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