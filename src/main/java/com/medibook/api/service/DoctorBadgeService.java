package com.medibook.api.service;
import com.medibook.api.dto.Badge.BadgeDTO;
import com.medibook.api.dto.Badge.DoctorBadgesResponseDTO;
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
public class DoctorBadgeService {

    // ========== BADGE THRESHOLD CONSTANTS ==========
    // Centralized threshold values matching DoctorBadgeStatisticsUpdateService
    
    // Rating-related thresholds
    private static final double EXCEPTIONAL_COMMUNICATOR_THRESHOLD = 0.15;
    private static final double EMPATHETIC_DOCTOR_THRESHOLD = 0.15;
    private static final double PUNCTUALITY_PROFESSIONAL_THRESHOLD = 0.12;
    private static final double PUNCTUALITY_CANCELLATION_MAX = 0.1;
    private static final double SUSTAINED_EXCELLENCE_AVG_RATING = 4.7;
    private static final int SUSTAINED_EXCELLENCE_MIN_RATINGS = 100;
    private static final double SUSTAINED_EXCELLENCE_LOW_SCORE_MAX = 0.05;
    
    // Documentation thresholds
    private static final double COMPLETE_DOCUMENTER_THRESHOLD = 0.8;
    private static final double DETAILED_HISTORIAN_THRESHOLD = 0.9;
    private static final double DETAILED_HISTORIAN_MIN_WORDS = 150.0;
    
    // Relationship thresholds
    private static final int RELATIONSHIP_BUILDER_MIN_PATIENTS = 50;
    private static final int RELATIONSHIP_BUILDER_RETURNING_MIN = 10;
    
    // Consistency thresholds
    private static final double CONSISTENT_PROFESSIONAL_CANCELLATION_MAX = 0.1;
    
    // Top specialist thresholds
    private static final int TOP_SPECIALIST_MIN_TURNS = 100;
    private static final double TOP_SPECIALIST_PERCENTILE = 0.1;
    
    // Medical legend thresholds
    private static final int MEDICAL_LEGEND_MIN_TURNS = 500;
    private static final double MEDICAL_LEGEND_AVG_RATING = 4.7;
    private static final int MEDICAL_LEGEND_MIN_OTHER_BADGES = 8;

    private final DoctorBadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final DoctorBadgeStatisticsRepository statisticsRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    
    private final RatingRepository ratingRepository;
    private final TurnAssignedRepository turnAssignedRepository;
    
    @Transactional(readOnly = true)
    public DoctorBadgesResponseDTO getDoctorBadges(UUID doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

        List<DoctorBadge> allBadges = badgeRepository.findByDoctor_IdOrderByEarnedAtDesc(doctorId);
        
        Map<BadgeType.BadgeCategory, List<BadgeDTO>> badgesByCategory = allBadges.stream()
                .map(this::toBadgeDTO)
                .collect(Collectors.groupingBy(
                    badge -> badge.getBadgeType().getCategory()
                ));

        long activeBadges = allBadges.stream().filter(DoctorBadge::getIsActive).count();

        return DoctorBadgesResponseDTO.builder()
                .doctorId(doctorId)
                .doctorName(doctor.getName() + " " + doctor.getSurname())
                .totalActiveBadges((int) activeBadges)
                .qualityOfCareBadges(badgesByCategory.getOrDefault(BadgeType.BadgeCategory.QUALITY_OF_CARE, new ArrayList<>()))
                .professionalismBadges(badgesByCategory.getOrDefault(BadgeType.BadgeCategory.PROFESSIONALISM, new ArrayList<>()))
                .consistencyBadges(badgesByCategory.getOrDefault(BadgeType.BadgeCategory.CONSISTENCY, new ArrayList<>()))
                .build();
    }


    @Transactional
    public void evaluateAllBadges(UUID doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

        evaluateExceptionalCommunicator(doctor);
        evaluateEmpatheticDoctor(doctor);
        evaluatePunctualityProfessional(doctor);
        evaluateSustainedExcellence(doctor);
        evaluateCompleteDocumenter(doctor);
        evaluateDetailedHistorian(doctor);
        evaluateAgileResponder(doctor);
        evaluateRelationshipBuilder(doctor);
        evaluateConsistentProfessional(doctor);
        evaluateAlwaysAvailable(doctor);
        evaluateTopSpecialist(doctor);
        evaluateMedicalLegend(doctor);
    }

    private void evaluateExceptionalCommunicator(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        
        if (stats.getTotalRatingsReceived() < 50) {
            deactivateBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR);
            return;
        }

        int requiredCount = (int) Math.ceil(50 * EXCEPTIONAL_COMMUNICATOR_THRESHOLD);
        boolean qualifies = stats.getLast50CommunicationCount() >= requiredCount;

        if (qualifies) {
            activateBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR);
        } else {
            deactivateBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR);
        }
    }

    private void evaluateEmpatheticDoctor(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        
        if (stats.getTotalRatingsReceived() < 50) {
            deactivateBadge(doctor, BadgeType.EMPATHETIC_DOCTOR);
            return;
        }

        int requiredCount = (int) Math.ceil(50 * EMPATHETIC_DOCTOR_THRESHOLD);
        boolean qualifies = stats.getLast50EmpathyCount() >= requiredCount;

        if (qualifies) {
            activateBadge(doctor, BadgeType.EMPATHETIC_DOCTOR);
        } else {
            deactivateBadge(doctor, BadgeType.EMPATHETIC_DOCTOR);
        }
    }

    private void evaluatePunctualityProfessional(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        
        if (stats.getTotalRatingsReceived() < 30 || stats.getTurnsLast90Days() < 20) {
            deactivateBadge(doctor, BadgeType.PUNCTUALITY_PROFESSIONAL);
            return;
        }

        int requiredPunctualityCount = (int) Math.ceil(50 * PUNCTUALITY_PROFESSIONAL_THRESHOLD);
        boolean hasPunctualityRatings = stats.getLast50PunctualityCount() >= requiredPunctualityCount;

        double cancellationRate = stats.getTurnsLast90Days() > 0 
            ? (double) stats.getCancellationsLast90Days() / stats.getTurnsLast90Days() 
            : 0.0;
        boolean lowCancellationRate = cancellationRate < PUNCTUALITY_CANCELLATION_MAX;

        if (hasPunctualityRatings && lowCancellationRate) {
            activateBadge(doctor, BadgeType.PUNCTUALITY_PROFESSIONAL);
        } else {
            deactivateBadge(doctor, BadgeType.PUNCTUALITY_PROFESSIONAL);
        }
    }

    private void evaluateSustainedExcellence(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        
        if (stats.getTotalTurnsCompleted() < 100) {
            deactivateBadge(doctor, BadgeType.SUSTAINED_EXCELLENCE);
            return;
        }

        List<TurnAssigned> last100 = turnAssignedRepository
                .findByDoctor_IdAndStatusOrderByScheduledAtDesc(doctor.getId(), "COMPLETED")
                .stream().limit(100).toList();
        
        List<Integer> scores = last100.stream()
                .map(turn -> ratingRepository.findByTurnAssigned_IdAndRater_Id(
                    turn.getId(), turn.getPatient().getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Rating::getScore)
                .toList();

        if (scores.isEmpty()) {
            deactivateBadge(doctor, BadgeType.SUSTAINED_EXCELLENCE);
            return;
        }

        double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        long lowScoreCount = scores.stream().filter(s -> s < 4).count();
        double lowScoreRate = (double) lowScoreCount / scores.size();

        if (avgScore >= SUSTAINED_EXCELLENCE_AVG_RATING && lowScoreRate < SUSTAINED_EXCELLENCE_LOW_SCORE_MAX) {
            activateBadge(doctor, BadgeType.SUSTAINED_EXCELLENCE);
        } else {
            deactivateBadge(doctor, BadgeType.SUSTAINED_EXCELLENCE);
        }
    }

    private void evaluateCompleteDocumenter(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        
        if (stats.getTotalTurnsCompleted() < 50) {
            deactivateBadge(doctor, BadgeType.COMPLETE_DOCUMENTER);
            return;
        }

        int requiredDocumented = (int) Math.ceil(50 * COMPLETE_DOCUMENTER_THRESHOLD);
        boolean qualifies = stats.getLast50DocumentedCount() >= requiredDocumented;

        if (qualifies) {
            activateBadge(doctor, BadgeType.COMPLETE_DOCUMENTER);
        } else {
            deactivateBadge(doctor, BadgeType.COMPLETE_DOCUMENTER);
        }
    }

    private void evaluateDetailedHistorian(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        
        if (stats.getTotalTurnsCompleted() < 30) {
            deactivateBadge(doctor, BadgeType.DETAILED_HISTORIAN);
            return;
        }

        int requiredDocumented = (int) Math.ceil(30 * DETAILED_HISTORIAN_THRESHOLD);
        boolean hasHighDocRate = stats.getLast30DocumentedCount() >= requiredDocumented;

        boolean hasDetailedEntries = stats.getLast30AvgWordsPerEntry() >= DETAILED_HISTORIAN_MIN_WORDS;

        boolean hasPrerequisite = badgeRepository.existsByDoctor_IdAndBadgeTypeAndIsActive(
                doctor.getId(), BadgeType.COMPLETE_DOCUMENTER, true);

        if (hasHighDocRate && hasDetailedEntries && hasPrerequisite) {
            activateBadge(doctor, BadgeType.DETAILED_HISTORIAN);
        } else {
            deactivateBadge(doctor, BadgeType.DETAILED_HISTORIAN);
        }
    }

    private void evaluateAgileResponder(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        
        if (stats.getLast10RequestsHandled() < 10) {
            deactivateBadge(doctor, BadgeType.AGILE_RESPONDER);
            return;
        }

        activateBadge(doctor, BadgeType.AGILE_RESPONDER);
    }

    private void evaluateRelationshipBuilder(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        
        boolean hasEnoughPatients = stats.getTotalUniquePatients() >= RELATIONSHIP_BUILDER_MIN_PATIENTS;
        boolean hasReturningPatients = stats.getReturningPatientsCount() >= RELATIONSHIP_BUILDER_RETURNING_MIN;

        if (hasEnoughPatients && hasReturningPatients) {
            activateBadge(doctor, BadgeType.RELATIONSHIP_BUILDER);
        } else {
            deactivateBadge(doctor, BadgeType.RELATIONSHIP_BUILDER);
        }
    }

    private void evaluateConsistentProfessional(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        
        if (stats.getTurnsLast90Days() < 20) {
            deactivateBadge(doctor, BadgeType.CONSISTENT_PROFESSIONAL);
            return;
        }

        double cancellationRate = (double) stats.getCancellationsLast90Days() / stats.getTurnsLast90Days();
        boolean lowCancellationRate = cancellationRate < CONSISTENT_PROFESSIONAL_CANCELLATION_MAX;

        boolean qualifies = lowCancellationRate;

        if (qualifies) {
            activateBadge(doctor, BadgeType.CONSISTENT_PROFESSIONAL);
        } else {
            deactivateBadge(doctor, BadgeType.CONSISTENT_PROFESSIONAL);
        }
    }

    private void evaluateAlwaysAvailable(User doctor) {

        Optional<DoctorProfile> profileOpt = doctorProfileRepository.findByUserId(doctor.getId());

        if (profileOpt.isEmpty()) {
            log.warn("[ALWAYS_AVAILABLE] No doctor profile found for doctor: {}. Deactivating badge.", doctor.getId());
            deactivateBadge(doctor, BadgeType.ALWAYS_AVAILABLE);
            return;
        }

        String availabilityJson = profileOpt.get().getAvailabilitySchedule();

        boolean hasAvailability = availabilityJson != null && !availabilityJson.isEmpty();

        if (hasAvailability) {
            activateBadge(doctor, BadgeType.ALWAYS_AVAILABLE);
        } else {
            deactivateBadge(doctor, BadgeType.ALWAYS_AVAILABLE);
        }

    }

    private void evaluateTopSpecialist(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        Optional<DoctorProfile> profileOpt = doctorProfileRepository.findByUserId(doctor.getId());
        
        if (profileOpt.isEmpty()) {
            deactivateBadge(doctor, BadgeType.TOP_SPECIALIST);
            return;
        }

        if (stats.getTotalTurnsCompleted() < TOP_SPECIALIST_MIN_TURNS) {
            deactivateBadge(doctor, BadgeType.TOP_SPECIALIST);
            return;
        }

        List<TurnAssigned> lastYearTurns = turnAssignedRepository
                .findByDoctor_IdAndStatusOrderByScheduledAtDesc(doctor.getId(), "COMPLETED")
                .stream()
                .filter(t -> t.getScheduledAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC).minusYears(1)))
                .toList();
        
        if (lastYearTurns.size() < 12) {
            deactivateBadge(doctor, BadgeType.TOP_SPECIALIST);
            return;
        }

        String specialty = profileOpt.get().getSpecialty();
        List<User> specialtyDoctors = userRepository.findDoctorsByStatusAndSpecialty("ACTIVE", specialty);
        
        if (doctor.getScore() == null) {
            deactivateBadge(doctor, BadgeType.TOP_SPECIALIST);
            return;
        }

        List<Double> specialtyScores = specialtyDoctors.stream()
                .map(User::getScore)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .toList();

        int top10Percent = Math.max(1, (int) Math.ceil(specialtyScores.size() * TOP_SPECIALIST_PERCENTILE));
        List<Double> top10PercentScores = specialtyScores.stream().limit(top10Percent).toList();

        if (top10PercentScores.contains(doctor.getScore())) {
            activateBadge(doctor, BadgeType.TOP_SPECIALIST);
        } else {
            deactivateBadge(doctor, BadgeType.TOP_SPECIALIST);
        }
    }

    private void evaluateMedicalLegend(User doctor) {
        DoctorBadgeStatistics stats = getOrCreateStatistics(doctor.getId());
        
        if (stats.getTotalTurnsCompleted() < MEDICAL_LEGEND_MIN_TURNS) {
            deactivateBadge(doctor, BadgeType.MEDICAL_LEGEND);
            return;
        }

        List<TurnAssigned> last200 = turnAssignedRepository
                .findByDoctor_IdAndStatusOrderByScheduledAtDesc(doctor.getId(), "COMPLETED")
                .stream().limit(200).toList();
        
        List<Integer> scores = last200.stream()
                .map(turn -> ratingRepository.findByTurnAssigned_IdAndRater_Id(
                    turn.getId(), turn.getPatient().getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Rating::getScore)
                .toList();

        if (scores.isEmpty()) {
            deactivateBadge(doctor, BadgeType.MEDICAL_LEGEND);
            return;
        }

        double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        if (avgScore < MEDICAL_LEGEND_AVG_RATING) {
            deactivateBadge(doctor, BadgeType.MEDICAL_LEGEND);
            return;
        }

        long activeBadges = badgeRepository.countActiveBadgesByDoctorIdExcludingType(
                doctor.getId(), BadgeType.MEDICAL_LEGEND);
        
        if (activeBadges < MEDICAL_LEGEND_MIN_OTHER_BADGES) {
            deactivateBadge(doctor, BadgeType.MEDICAL_LEGEND);
            return;
        }

        boolean activeRecently = stats.getTurnsLast90Days() > 0;

        if (activeRecently) {
            activateBadge(doctor, BadgeType.MEDICAL_LEGEND);
        } else {
            deactivateBadge(doctor, BadgeType.MEDICAL_LEGEND);
        }
    }


    private DoctorBadgeStatistics getOrCreateStatistics(UUID doctorId) {
        return statisticsRepository.findByDoctorId(doctorId)
                .orElseGet(() -> {
                    User doctor = userRepository.findById(doctorId)
                            .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
                    
                    DoctorBadgeStatistics stats = DoctorBadgeStatistics.builder()
                            .doctor(doctor)
                            .totalRatingsReceived(0)
                            .last50CommunicationCount(0)
                            .last50EmpathyCount(0)
                            .last50PunctualityCount(0)
                            .totalTurnsCompleted(0)
                            .totalTurnsCancelled(0)
                            .turnsLast90Days(0)
                            .cancellationsLast90Days(0)
                            .last50DocumentedCount(0)
                            .last30DocumentedCount(0)
                            .last30TotalWords(0)
                            .last30AvgWordsPerEntry(0.0)
                            .totalUniquePatients(0)
                            .returningPatientsCount(0)
                            .last10RequestsHandled(0)
                            .specialtyRankPercentile(null)
                            .build();
                    
                    return statisticsRepository.save(stats);
                });
    }

    private void activateBadge(User doctor, BadgeType badgeType) {

        Optional<DoctorBadge> existing = badgeRepository.findByDoctor_IdAndBadgeType(
                doctor.getId(), badgeType);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (existing.isPresent()) {
            DoctorBadge badge = existing.get();
            if (!badge.getIsActive()) {
                badge.setIsActive(true);
                badge.setLastEvaluatedAt(now);
                badgeRepository.save(badge);
            } else {
                badge.setLastEvaluatedAt(now);
                badgeRepository.save(badge);
            }
        } else {
            DoctorBadge newBadge = DoctorBadge.builder()
                    .doctor(doctor)
                    .badgeType(badgeType)
                    .earnedAt(now)
                    .isActive(true)
                    .lastEvaluatedAt(now)
                    .build();
            badgeRepository.save(newBadge);
        }
    }

    private void deactivateBadge(User doctor, BadgeType badgeType) {

        Optional<DoctorBadge> existing = badgeRepository.findByDoctor_IdAndBadgeType(
                doctor.getId(), badgeType);

        if (existing.isPresent()) {
            DoctorBadge badge = existing.get();
            if (existing.get().getIsActive()) {
                badge.setIsActive(false);
                badge.setLastEvaluatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                badgeRepository.save(badge);
            } else {
                badge.setLastEvaluatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                badgeRepository.save(badge);
            }
        } else {
        }
    }

    private BadgeDTO toBadgeDTO(DoctorBadge badge) {
        return BadgeDTO.builder()
                .badgeType(badge.getBadgeType())
                .category(badge.getBadgeType().getCategory().name())
                .isActive(badge.getIsActive())
                .earnedAt(badge.getEarnedAt())
                .lastEvaluatedAt(badge.getLastEvaluatedAt())
                .build();
    }



    @Transactional
    public void evaluateRatingRelatedBadges(UUID doctorId) {
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

        evaluateExceptionalCommunicator(doctor);
        evaluateEmpatheticDoctor(doctor);
    
        evaluateSustainedExcellence(doctor);
    
    

    }


    @Transactional
    public void evaluateDocumentationRelatedBadges(UUID doctorId) {
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

        evaluateCompleteDocumenter(doctor);
        evaluateDetailedHistorian(doctor);
    

    }


    @Transactional
    public void evaluateConsistencyRelatedBadges(UUID doctorId) {
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

    
        evaluateConsistentProfessional(doctor);
    

    }


    @Transactional
    public void evaluateResponseRelatedBadges(UUID doctorId) {
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

        evaluateAgileResponder(doctor);

    }


    @Transactional
    public void evaluateTurnCompletionRelatedBadges(UUID doctorId) {
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

        evaluateRelationshipBuilder(doctor);
        evaluateTopSpecialist(doctor);
        evaluateMedicalLegend(doctor);

    }

    @Transactional
    public void evaluateAlwaysAvailableBadge(UUID doctorId) {
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

        evaluateAlwaysAvailable(doctor);

    }
}
