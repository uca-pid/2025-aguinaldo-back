package com.medibook.api.service;
import com.medibook.api.entity.PatientBadgeStatistics;
import com.medibook.api.entity.PatientBadgeType;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.repository.PatientBadgeRepository;
import com.medibook.api.repository.PatientBadgeStatisticsRepository;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.RatingRepository;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientBadgeStatisticsUpdateService {

    private final PatientBadgeStatisticsRepository statisticsRepository;
    private final UserRepository userRepository;
    private final TurnAssignedRepository turnAssignedRepository;
    private final RatingRepository ratingRepository;
    private final PatientBadgeRepository badgeRepository;

    @Async
    @Transactional
    public void updateAfterTurnCompleted(UUID patientId, UUID doctorId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementTurnCompleted(patientId);

            updateDoctorRelationshipStats(patientId, doctorId);
            updateTimeBasedStats(patientId);

        } catch (Exception e) {
            log.error("Error updating turn completion statistics for patient {}", patientId, e);
        }
    }

    @Async
    @Transactional
    public void updateAfterTurnCancelled(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementTurnCancelled(patientId);
        } catch (Exception e) {
            log.error("Error updating cancellation statistics for patient {}", patientId, e);
        }
    }

    @Async
    @Transactional
    public void updateAfterTurnNoShow(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementTurnNoShow(patientId);
        } catch (Exception e) {
            log.error("Error updating no-show statistics for patient {}", patientId, e);
        }
    }

    @Async
    @Transactional
    public void updateAfterRatingGiven(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementRatingGiven(patientId);
            updateAverageRatingGiven(patientId);
        } catch (Exception e) {
            log.error("Error updating rating given statistics for patient {}", patientId, e);
        }
    }

    @Async
    @Transactional
    public void updateAfterRatingReceived(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementRatingReceived(patientId);
            updateAverageRatingReceived(patientId);
        } catch (Exception e) {
            log.error("Error updating rating received statistics for patient {}", patientId, e);
        }
    }

    @Async
    @Transactional
    public void updateAfterFileUploaded(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementFileUploaded(patientId);
        } catch (Exception e) {
            log.error("Error updating file upload statistics for patient {}", patientId, e);
        }
    }

    @Async
    @Transactional
    public void updateAfterAdvanceBooking(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementAdvanceBooking(patientId);
        } catch (Exception e) {
            log.error("Error updating advance booking statistics for patient {}", patientId, e);
        }
    }

    @Async
    @Transactional
    public void updateAfterPunctualityRating(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementPunctualRating(patientId);
        } catch (Exception e) {
            log.error("Error updating punctuality rating statistics for patient {}", patientId, e);
        }
    }

    @Async
    @Transactional
    public void updateAfterCollaborationRating(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementCollaborationRating(patientId);
        } catch (Exception e) {
            log.error("Error updating collaboration rating statistics for patient {}", patientId, e);
        }
    }

    @Async
    @Transactional
    public void updateAfterFollowInstructionsRating(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementFollowInstructionsRating(patientId);
        } catch (Exception e) {
            log.error("Error updating follow instructions rating statistics for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateAfterTurnCompletedSync(UUID patientId, UUID doctorId) {
        try {
            ensureStatisticsExist(patientId);
            PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElseThrow();
            stats.setTotalTurnsCompleted(stats.getTotalTurnsCompleted() + 1);
            stats.setTurnsLast12Months(stats.getTurnsLast12Months() + 1);
            stats.setTurnsLast6Months(stats.getTurnsLast6Months() + 1);
            stats.setTurnsLast90Days(stats.getTurnsLast90Days() + 1);
            statisticsRepository.save(stats);

            updateDoctorRelationshipStats(patientId, doctorId);
            updateTimeBasedStats(patientId);

        } catch (Exception e) {
            log.error("Error updating turn completion statistics for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateAfterTurnCancelledSync(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementTurnCancelled(patientId);
        } catch (Exception e) {
            log.error("Error updating cancellation statistics for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateAfterTurnNoShowSync(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementTurnNoShow(patientId);
        } catch (Exception e) {
            log.error("Error updating no-show statistics for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateAfterRatingGivenSync(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementRatingGiven(patientId);
            updateAverageRatingGiven(patientId);
        } catch (Exception e) {
            log.error("Error updating rating given statistics for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateAfterRatingReceivedSync(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementRatingReceived(patientId);
            updateAverageRatingReceived(patientId);
        } catch (Exception e) {
            log.error("Error updating rating received statistics for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateAfterFileUploadedSync(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementFileUploaded(patientId);
        } catch (Exception e) {
            log.error("Error updating file upload statistics for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateAfterAdvanceBookingSync(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementAdvanceBooking(patientId);
        } catch (Exception e) {
            log.error("Error updating advance booking statistics for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateAfterPunctualityRatingSync(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementPunctualRating(patientId);
        } catch (Exception e) {
            log.error("Error updating punctuality rating statistics for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateAfterCollaborationRatingSync(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementCollaborationRating(patientId);
        } catch (Exception e) {
            log.error("Error updating collaboration rating statistics for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateAfterFollowInstructionsRatingSync(UUID patientId) {
        try {
            ensureStatisticsExist(patientId);
            statisticsRepository.incrementFollowInstructionsRating(patientId);
        } catch (Exception e) {
            log.error("Error updating follow instructions rating statistics for patient {}", patientId, e);
        }
    }

    private void ensureStatisticsExist(UUID patientId) {
        if (statisticsRepository.findByPatientId(patientId).isEmpty()) {
            User patient = userRepository.findById(patientId)
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

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
                    .avgRatingGiven(0.0)
                    .avgRatingReceived(0.0)
                    .totalUniqueDoctors(0)
                    .turnsWithSameDoctorLast12Months(0)
                    .differentSpecialtiesLast12Months(0)
                    .build();

            statisticsRepository.save(stats);
        }
    }

    private void updateDoctorRelationshipStats(UUID patientId, UUID doctorId) {
        try {
            PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElseThrow();

            long previousTurnsWithDoctor = turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(
                    doctorId, patientId, "COMPLETED");

            if (previousTurnsWithDoctor == 1) {
                stats.setTotalUniqueDoctors(stats.getTotalUniqueDoctors() + 1);
            }

            @SuppressWarnings("unchecked")
            List<TurnAssigned> turnsWithDoctor = turnAssignedRepository.findByPatient_IdAndStatusOrderByScheduledAtDesc(patientId, "COMPLETED")
                    .stream()
                    .filter(t -> t.getDoctor().getId().equals(doctorId) && t.getScheduledAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC).minusYears(1)))
                    .toList();

            stats.setTurnsWithSameDoctorLast12Months(turnsWithDoctor.size());

            statisticsRepository.save(stats);
        } catch (Exception e) {
            log.error("Error updating doctor relationship stats for patient {}", patientId, e);
        }
    }

    private void updateTimeBasedStats(UUID patientId) {
        try {
            PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElseThrow();
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

            long turnsLast12Months = turnAssignedRepository
                    .findByPatient_IdAndStatusOrderByScheduledAtDesc(patientId, "COMPLETED")
                    .stream()
                    .filter(t -> t.getScheduledAt().isAfter(now.minusYears(1)))
                    .count();

            long turnsLast6Months = turnAssignedRepository
                    .findByPatient_IdAndStatusOrderByScheduledAtDesc(patientId, "COMPLETED")
                    .stream()
                    .filter(t -> t.getScheduledAt().isAfter(now.minusMonths(6)))
                    .count();

            long turnsLast90Days = turnAssignedRepository
                    .findByPatient_IdAndStatusOrderByScheduledAtDesc(patientId, "COMPLETED")
                    .stream()
                    .filter(t -> t.getScheduledAt().isAfter(now.minusDays(90)))
                    .count();

            stats.setTurnsLast12Months((int) turnsLast12Months);
            stats.setTurnsLast6Months((int) turnsLast6Months);
            stats.setTurnsLast90Days((int) turnsLast90Days);

            long differentSpecialties = turnAssignedRepository
                    .findByPatient_IdAndStatusOrderByScheduledAtDesc(patientId, "COMPLETED")
                    .stream()
                    .filter(t -> t.getScheduledAt().isAfter(now.minusYears(1)))
                    .map(turn -> turn.getDoctor().getDoctorProfile() != null ?
                            turn.getDoctor().getDoctorProfile().getSpecialty() : null)
                    .filter(specialty -> specialty != null)
                    .distinct()
                    .count();

            stats.setDifferentSpecialtiesLast12Months((int) differentSpecialties);

            statisticsRepository.save(stats);
        } catch (Exception e) {
            log.error("Error updating time-based stats for patient {}", patientId, e);
        }
    }

    private void updateAverageRatingGiven(UUID patientId) {
        try {
            List<com.medibook.api.entity.Rating> allRatings = ratingRepository.findAllOrderByCreatedAtDesc();
            List<com.medibook.api.entity.Rating> ratingsGiven = allRatings.stream()
                    .filter(r -> r.getRater() != null && r.getRater().getId().equals(patientId))
                    .toList();

            if (!ratingsGiven.isEmpty()) {
                double avgRating = ratingsGiven.stream()
                        .mapToInt(com.medibook.api.entity.Rating::getScore)
                        .average()
                        .orElse(0.0);

                PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElseThrow();
                stats.setAvgRatingGiven(avgRating);
                stats.setTotalRatingsGiven(ratingsGiven.size());
                statisticsRepository.save(stats);
            }
        } catch (Exception e) {
            log.error("Error updating average rating given for patient {}", patientId, e);
        }
    }

    private void updateAverageRatingReceived(UUID patientId) {
        try {
            List<com.medibook.api.entity.Rating> allRatings = ratingRepository.findAllOrderByCreatedAtDesc();
            List<com.medibook.api.entity.Rating> ratingsReceived = allRatings.stream()
                    .filter(r -> r.getRated() != null && r.getRated().getId().equals(patientId))
                    .toList();

            if (!ratingsReceived.isEmpty()) {
                double avgRating = ratingsReceived.stream()
                        .mapToInt(com.medibook.api.entity.Rating::getScore)
                        .average()
                        .orElse(0.0);

                PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElseThrow();
                stats.setAvgRatingReceived(avgRating);
                stats.setTotalRatingsReceived(ratingsReceived.size());
                statisticsRepository.save(stats);
            }
        } catch (Exception e) {
            log.error("Error updating average rating received for patient {}", patientId, e);
        }
    }

    @Transactional
    public void updateProgressAfterTurnCompletion(UUID patientId) {
        PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElse(null);
        if (stats == null) return;

        List<PatientBadgeType> earnedBadges = getEarnedBadges(patientId);

        stats.setProgressMediBookWelcome(calculateMediBookWelcomeProgress(stats, earnedBadges));
        stats.setProgressHealthGuardian(calculateHealthGuardianProgress(stats, earnedBadges));
        stats.setProgressCommittedPatient(calculateCommittedPatientProgress(stats, earnedBadges));
        stats.setProgressContinuousFollowup(calculateContinuousFollowupProgress(stats, earnedBadges));
        stats.setProgressConstantPatient(calculateConstantPatientProgress(stats, earnedBadges));
        stats.setProgressExcellenceModel(calculateExcellenceModelProgress(stats, earnedBadges));

        statisticsRepository.save(stats);
    }

    @Transactional
    public void updateProgressAfterRating(UUID patientId) {
        PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElse(null);
        if (stats == null) return;

        List<PatientBadgeType> earnedBadges = getEarnedBadges(patientId);

        stats.setProgressExemplaryPunctuality(calculateExemplaryPunctualityProgress(stats, earnedBadges));
        stats.setProgressExcellentCollaborator(calculateExcellentCollaboratorProgress(stats, earnedBadges));
        stats.setProgressResponsibleEvaluator(calculateResponsibleEvaluatorProgress(stats, earnedBadges));
        stats.setProgressExcellenceModel(calculateExcellenceModelProgress(stats, earnedBadges));

        statisticsRepository.save(stats);
    }

    @Transactional
    public void updateProgressAfterFileUpload(UUID patientId) {
        PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElse(null);
        if (stats == null) return;

        List<PatientBadgeType> earnedBadges = getEarnedBadges(patientId);

        stats.setProgressAlwaysPrepared(calculateAlwaysPreparedProgress(stats, earnedBadges));

        statisticsRepository.save(stats);
    }

    @Transactional
    public void updateProgressAfterBooking(UUID patientId) {
        PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElse(null);
        if (stats == null) return;

        List<PatientBadgeType> earnedBadges = getEarnedBadges(patientId);

        stats.setProgressSmartPlanner(calculateSmartPlannerProgress(stats, earnedBadges));

        statisticsRepository.save(stats);
    }

    @Transactional
    public void updateAllBadgeProgress(UUID patientId) {
        PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElse(null);
        if (stats == null) return;

        List<PatientBadgeType> earnedBadges = getEarnedBadges(patientId);

        stats.setProgressMediBookWelcome(calculateMediBookWelcomeProgress(stats, earnedBadges));
        stats.setProgressHealthGuardian(calculateHealthGuardianProgress(stats, earnedBadges));
        stats.setProgressCommittedPatient(calculateCommittedPatientProgress(stats, earnedBadges));
        stats.setProgressContinuousFollowup(calculateContinuousFollowupProgress(stats, earnedBadges));
        stats.setProgressConstantPatient(calculateConstantPatientProgress(stats, earnedBadges));
        stats.setProgressExemplaryPunctuality(calculateExemplaryPunctualityProgress(stats, earnedBadges));
        stats.setProgressSmartPlanner(calculateSmartPlannerProgress(stats, earnedBadges));
        stats.setProgressExcellentCollaborator(calculateExcellentCollaboratorProgress(stats, earnedBadges));
        stats.setProgressAlwaysPrepared(calculateAlwaysPreparedProgress(stats, earnedBadges));
        stats.setProgressResponsibleEvaluator(calculateResponsibleEvaluatorProgress(stats, earnedBadges));
        stats.setProgressExcellenceModel(calculateExcellenceModelProgress(stats, earnedBadges));

        statisticsRepository.save(stats);
    }

    private List<PatientBadgeType> getEarnedBadges(UUID patientId) {
        return badgeRepository.findByPatient_IdAndIsActiveTrue(patientId)
                .stream()
                .map(badge -> badge.getBadgeType())
                .toList();
    }

    private double calculateMediBookWelcomeProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.MEDIBOOK_WELCOME)) return 100.0;

        int turnsCompleted = stats.getTotalTurnsCompleted();
        return turnsCompleted >= 1 ? 100.0 : ((double) turnsCompleted / 1) * 100;
    }

    private double calculateHealthGuardianProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.HEALTH_GUARDIAN)) return 100.0;

        int turnsLast6Months = stats.getTurnsLast6Months();
        return Math.min(((double) turnsLast6Months / 3) * 100, 100.0);
    }

    private double calculateCommittedPatientProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.COMMITTED_PATIENT)) return 100.0;

        Double attendanceRate = stats.getLast5TurnsAttendanceRate();
        return attendanceRate != null ? Math.min(attendanceRate * 100, 100.0) : 0.0;
    }

    private double calculateContinuousFollowupProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.CONTINUOUS_FOLLOWUP)) return 100.0;

        int turnsWithSameDoctor = stats.getTurnsWithSameDoctorLast12Months();
        return Math.min(((double) turnsWithSameDoctor / 3) * 100, 100.0);
    }

    private double calculateConstantPatientProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.CONSTANT_PATIENT)) return 100.0;

        int totalTurns = stats.getTotalTurnsCompleted();
        int turnsLast6Months = stats.getTurnsLast6Months();

        double totalProgress = Math.min(((double) totalTurns / 15) * 100, 100);
        double recentProgress = turnsLast6Months > 0 ? 100.0 : 0.0;

        return Math.min((totalProgress + recentProgress) / 2, 100.0);
    }

    private double calculateExemplaryPunctualityProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.EXEMPLARY_PUNCTUALITY)) return 100.0;

        int punctualCount = stats.getLast10TurnsPunctualCount();
        return Math.min(((double) punctualCount / 8) * 100, 100.0);
    }

    private double calculateSmartPlannerProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.SMART_PLANNER)) return 100.0;

        int advanceBookings = stats.getLast5TurnsAdvanceBookingCount();
        return Math.min(((double) advanceBookings / 3.5) * 100, 100.0);
    }

    private double calculateExcellentCollaboratorProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.EXCELLENT_COLLABORATOR)) return 100.0;

        int collaborationCount = stats.getLast15TurnsCollaborationCount();
        int followInstructionsCount = stats.getLast15TurnsFollowInstructionsCount();

        double collaborationProgress = Math.min(((double) collaborationCount / 10.5) * 100, 100);
        double followProgress = Math.min(((double) followInstructionsCount / 10.5) * 100, 100);

        return Math.min((collaborationProgress + followProgress) / 2, 100.0);
    }

    private double calculateAlwaysPreparedProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.ALWAYS_PREPARED)) return 100.0;

        int uploadedCount = stats.getLast10TurnsFilesUploadedCount();
        return Math.min(((double) uploadedCount / 7) * 100, 100.0);
    }

    private double calculateResponsibleEvaluatorProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.RESPONSIBLE_EVALUATOR)) return 100.0;

        int ratingsGiven = stats.getTotalRatingsGiven();
        return Math.min(((double) ratingsGiven / 8) * 100, 100.0);
    }

    private double calculateExcellenceModelProgress(PatientBadgeStatistics stats, List<PatientBadgeType> earnedBadges) {
        if (earnedBadges.contains(PatientBadgeType.EXCELLENCE_MODEL)) return 100.0;

        long otherBadges = earnedBadges.stream()
                .filter(badge -> badge != PatientBadgeType.EXCELLENCE_MODEL)
                .count();

        int turnsCompleted = stats.getTotalTurnsCompleted();
        Double avgRatingReceived = stats.getAvgRatingReceived();
        int turnsLast90Days = stats.getTurnsLast90Days();

        double badgeProgress = Math.min(((double) otherBadges / 4) * 100, 100);
        double turnProgress = Math.min(((double) turnsCompleted / 25) * 100, 100);
        double ratingProgress = avgRatingReceived != null && avgRatingReceived >= 4.0 ? 100.0 : 0.0;
        double activityProgress = turnsLast90Days > 0 ? 100.0 : 0.0;

        return Math.min((badgeProgress + turnProgress + ratingProgress + activityProgress) / 4, 100.0);
    }

    @Transactional
    public void updateProgressAfterTurnCompletionSync(UUID patientId) {
        PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElse(null);
        if (stats == null) return;

        List<PatientBadgeType> earnedBadges = getEarnedBadges(patientId);

        stats.setProgressMediBookWelcome(calculateMediBookWelcomeProgress(stats, earnedBadges));
        stats.setProgressHealthGuardian(calculateHealthGuardianProgress(stats, earnedBadges));
        stats.setProgressCommittedPatient(calculateCommittedPatientProgress(stats, earnedBadges));
        stats.setProgressContinuousFollowup(calculateContinuousFollowupProgress(stats, earnedBadges));
        stats.setProgressConstantPatient(calculateConstantPatientProgress(stats, earnedBadges));
        stats.setProgressExcellenceModel(calculateExcellenceModelProgress(stats, earnedBadges));

        statisticsRepository.save(stats);
    }

    @Transactional
    public void updateProgressAfterRatingSync(UUID patientId) {
        PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElse(null);
        if (stats == null) return;

        List<PatientBadgeType> earnedBadges = getEarnedBadges(patientId);

        stats.setProgressExemplaryPunctuality(calculateExemplaryPunctualityProgress(stats, earnedBadges));
        stats.setProgressExcellentCollaborator(calculateExcellentCollaboratorProgress(stats, earnedBadges));
        stats.setProgressResponsibleEvaluator(calculateResponsibleEvaluatorProgress(stats, earnedBadges));
        stats.setProgressExcellenceModel(calculateExcellenceModelProgress(stats, earnedBadges));

        statisticsRepository.save(stats);
    }

    @Transactional
    public void updateProgressAfterFileUploadSync(UUID patientId) {
        PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElse(null);
        if (stats == null) return;

        List<PatientBadgeType> earnedBadges = getEarnedBadges(patientId);

        stats.setProgressAlwaysPrepared(calculateAlwaysPreparedProgress(stats, earnedBadges));

        statisticsRepository.save(stats);
    }

    @Transactional
    public void updateProgressAfterBookingSync(UUID patientId) {
        PatientBadgeStatistics stats = statisticsRepository.findByPatientId(patientId).orElse(null);
        if (stats == null) return;

        List<PatientBadgeType> earnedBadges = getEarnedBadges(patientId);

        stats.setProgressSmartPlanner(calculateSmartPlannerProgress(stats, earnedBadges));

        statisticsRepository.save(stats);
    }
}