package com.medibook.api.service;
import com.medibook.api.entity.BadgeType;
import com.medibook.api.entity.DoctorBadgeStatistics;
import com.medibook.api.entity.User;
import com.medibook.api.repository.DoctorBadgeRepository;
import com.medibook.api.repository.DoctorBadgeStatisticsRepository;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.RatingRepository;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorBadgeStatisticsUpdateService {
    
    private static final double EXCELLENCE_RATING_THRESHOLD = 4.7;
    private static final int EXCELLENCE_MIN_RATINGS = 100;
    
    private static final int EMPATHY_COUNT_THRESHOLD = 15;
    private static final int EMPATHY_MIN_RATINGS = 30;
    
    private static final int COMMUNICATION_COUNT_THRESHOLD = 15;
    private static final int COMMUNICATION_MIN_RATINGS = 30;
    
    private static final int PUNCTUALITY_COUNT_THRESHOLD = 12;
    private static final int PUNCTUALITY_MIN_RATINGS = 30;
    
    private static final double DOCUMENTATION_RATE_THRESHOLD = 96.0;
    private static final double DETAILED_AVG_WORDS_THRESHOLD = 150.0;
    private static final int DETAILED_MIN_DOCUMENTED = 20;
    
    private static final double CONSISTENT_CANCELLATION_RATE_THRESHOLD = 10.0;
    private static final int CONSISTENT_MIN_COMPLETED = 20;
    
    private static final int RELATIONSHIP_RETURNING_THRESHOLD = 10;
    private static final int RELATIONSHIP_UNIQUE_THRESHOLD = 50;
    
    private static final double TOP_SPECIALIST_PERCENTILE_THRESHOLD = 10.0;
    private static final int TOP_SPECIALIST_MIN_COMPLETED = 100;
    
    private static final int LEGEND_MIN_COMPLETED = 500;
    private static final double LEGEND_RATING_THRESHOLD = 4.7;
    
    private final DoctorBadgeStatisticsRepository statisticsRepository;
    private final UserRepository userRepository;
    private final TurnAssignedRepository turnAssignedRepository;
    private final RatingRepository ratingRepository;
    private final DoctorBadgeRepository badgeRepository;
    
    @Async
    @Transactional
    public void updateAfterRatingAdded(UUID doctorId, Integer communicationScore, Integer empathyScore, Integer punctualityScore) {
        try {
            ensureStatisticsExist(doctorId);
            
            boolean hasCommunication = communicationScore != null && communicationScore >= 4;
            boolean hasEmpathy = empathyScore != null && empathyScore >= 4;
            boolean hasPunctuality = punctualityScore != null && punctualityScore >= 4;
            
            statisticsRepository.incrementRatingCounters(doctorId, hasCommunication, hasEmpathy, hasPunctuality);
            
            DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId).orElseThrow();
            if (stats.getTotalRatingsReceived() > 50 && stats.getTotalRatingsReceived() % 10 == 0) {
                recalculateLast50Ratings(doctorId);
            }
            
            log.debug("Updated rating statistics for doctor {}", doctorId);
        } catch (Exception e) {
            log.error("Error updating rating statistics for doctor {}", doctorId, e);
        }
    }
    
    @Async
    @Transactional
    public void updateAfterTurnCompleted(UUID doctorId, UUID patientId) {
        try {
            ensureStatisticsExist(doctorId);
            statisticsRepository.incrementTurnCompleted(doctorId);
            
            updatePatientRelationshipStats(doctorId, patientId);
            
            log.debug("Updated turn completion statistics for doctor {}", doctorId);
        } catch (Exception e) {
            log.error("Error updating turn completion statistics for doctor {}", doctorId, e);
        }
    }
    
    @Async
    @Transactional
    public void updateAfterTurnCancelled(UUID doctorId) {
        try {
            ensureStatisticsExist(doctorId);
            statisticsRepository.incrementTurnCancelled(doctorId);
            log.debug("Updated cancellation statistics for doctor {}", doctorId);
        } catch (Exception e) {
            log.error("Error updating cancellation statistics for doctor {}", doctorId, e);
        }
    }
    
    @Async
    @Transactional
    public void updateAfterMedicalHistoryDocumented(UUID doctorId, String content) {
        try {
            ensureStatisticsExist(doctorId);
            statisticsRepository.incrementDocumentation(doctorId);
            
            int wordCount = content != null ? content.split("\\s+").length : 0;
            updateWordCountStatistics(doctorId, wordCount);
            
            log.debug("Updated documentation statistics for doctor {}", doctorId);
        } catch (Exception e) {
            log.error("Error updating documentation statistics for doctor {}", doctorId, e);
        }
    }
    
    @Async
    @Transactional
    public void updateAfterModifyRequestHandled(UUID doctorId) {
        try {
            ensureStatisticsExist(doctorId);
            statisticsRepository.incrementRequestHandled(doctorId);
            log.debug("Updated request handling statistics for doctor {}", doctorId);
        } catch (Exception e) {
            log.error("Error updating request handling statistics for doctor {}", doctorId, e);
        }
    }
    
    private void ensureStatisticsExist(UUID doctorId) {
        if (statisticsRepository.findByDoctorId(doctorId).isEmpty()) {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
            
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
            
            statisticsRepository.save(stats);
            log.info("Created new statistics record for doctor {}", doctorId);
        }
    }
    
    private void recalculateLast50Ratings(UUID doctorId) {
        try {
            List<com.medibook.api.entity.Rating> last50Ratings = ratingRepository
                    .findTop50ByRatedIdOrderByCreatedAtDesc(doctorId);
            
            if (last50Ratings.isEmpty()) {
                return;
            }
            
            int commCount = 0;
            int empCount = 0;
            int punctCount = 0;
            
            for (com.medibook.api.entity.Rating rating : last50Ratings) {
                if (rating.getScore() >= 4 && rating.getSubcategory() != null) {
                    String subcategory = rating.getSubcategory().toLowerCase();
                    if (containsCommunicationKeywords(subcategory)) {
                        commCount++;
                    }
                    if (containsEmpathyKeywords(subcategory)) {
                        empCount++;
                    }
                    if (containsPunctualityKeywords(subcategory)) {
                        punctCount++;
                    }
                }
            }
            
            DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId).orElseThrow();
            stats.setLast50CommunicationCount(commCount);
            stats.setLast50EmpathyCount(empCount);
            stats.setLast50PunctualityCount(punctCount);
            statisticsRepository.save(stats);
            
            log.info("Recalculated last 50 ratings for doctor {} (comm: {}, emp: {}, punct: {})", 
                     doctorId, commCount, empCount, punctCount);
        } catch (Exception e) {
            log.error("Error recalculating last 50 ratings for doctor {}", doctorId, e);
        }
    }
    
    private void updatePatientRelationshipStats(UUID doctorId, UUID patientId) {
        try {
            DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId).orElseThrow();
            
            long previousTurns = turnAssignedRepository.countByDoctorIdAndPatientIdAndStatus(
                    doctorId, patientId, "COMPLETED");
            
            if (previousTurns == 1) {
                stats.setTotalUniquePatients(stats.getTotalUniquePatients() + 1);
                log.debug("New unique patient for doctor {}: total now {}", 
                         doctorId, stats.getTotalUniquePatients());
            } else if (previousTurns == 2) {
                stats.setReturningPatientsCount(stats.getReturningPatientsCount() + 1);
                log.debug("Returning patient for doctor {}: total now {}", 
                         doctorId, stats.getReturningPatientsCount());
            }
            
            statisticsRepository.save(stats);
        } catch (Exception e) {
            log.error("Error updating patient relationship stats for doctor {}", doctorId, e);
        }
    }
    
    private boolean containsCommunicationKeywords(String subcategory) {
        return subcategory.contains("explica") || 
               subcategory.contains("escucha") || 
               subcategory.contains("claramente") ||
               subcategory.contains("comunica");
    }
    
    private boolean containsEmpathyKeywords(String subcategory) {
        return subcategory.contains("empatía") || 
               subcategory.contains("empat") || 
               subcategory.contains("confianza") || 
               subcategory.contains("atención") ||
               subcategory.contains("amable");
    }
    
    private boolean containsPunctualityKeywords(String subcategory) {
        return subcategory.contains("horarios") || 
               subcategory.contains("respeta horarios") || 
               subcategory.contains("tiempo de espera") ||
               subcategory.contains("puntual");
    }
    
    private void updateWordCountStatistics(UUID doctorId, int newWordCount) {
        DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId).orElseThrow();
        
        stats.setLast30TotalWords(stats.getLast30TotalWords() + newWordCount);
        
        int documentedCount = stats.getLast30DocumentedCount();
        if (documentedCount > 0) {
            stats.setLast30AvgWordsPerEntry((double) stats.getLast30TotalWords() / documentedCount);
        }
        
        statisticsRepository.save(stats);
    }
    
    @Transactional
    public void updateProgressAfterRating(UUID doctorId) {
        DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId).orElse(null);
        if (stats == null) return;
        
        List<BadgeType> earnedBadges = getEarnedBadges(doctorId);
        
        stats.setProgressExcellenceInCare(calculateExcellenceProgress(stats, earnedBadges));
        stats.setProgressEmpathyChampion(calculateEmpathyProgress(stats, earnedBadges));
        stats.setProgressClearCommunicator(calculateCommunicatorProgress(stats, earnedBadges));
        stats.setProgressTimelyProfessional(calculateTimelyProgress(stats, earnedBadges));
        stats.setProgressTopSpecialist(calculateTopSpecialistProgress(stats, earnedBadges));
        stats.setProgressMedicalLegend(calculateLegendProgress(stats, earnedBadges));
        
        statisticsRepository.save(stats);
    }
    
    @Transactional
    public void updateProgressAfterTurnCompletion(UUID doctorId) {
        DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId).orElse(null);
        if (stats == null) return;
        
        List<BadgeType> earnedBadges = getEarnedBadges(doctorId);
        
        stats.setProgressRelationshipBuilder(calculateRelationshipProgress(stats, earnedBadges));
        stats.setProgressTopSpecialist(calculateTopSpecialistProgress(stats, earnedBadges));
        stats.setProgressMedicalLegend(calculateLegendProgress(stats, earnedBadges));
        
        statisticsRepository.save(stats);
    }

    @Transactional
    public void updateProgressAfterMedicalHistory(UUID doctorId) {
        DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId).orElse(null);
        if (stats == null) return;
        
        List<BadgeType> earnedBadges = getEarnedBadges(doctorId);
        
        stats.setProgressReliableExpert(calculateReliableProgress(stats, earnedBadges));
        stats.setProgressDetailedDiagnostician(calculateDetailedDiagnostProgress(stats, earnedBadges));
        stats.setProgressMedicalLegend(calculateLegendProgress(stats, earnedBadges));
        
        statisticsRepository.save(stats);
    }
    
    @Transactional
    public void updateProgressAfterModifyRequest(UUID doctorId) {
        DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId).orElse(null);
        if (stats == null) return;
        
        List<BadgeType> earnedBadges = getEarnedBadges(doctorId);
        
        stats.setProgressAgileResponder(calculateAgileProgress(stats, earnedBadges));
        
        statisticsRepository.save(stats);
    }
    
    @Transactional
    public void updateProgressAfterCancellation(UUID doctorId) {
        DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId).orElse(null);
        if (stats == null) return;
        
        List<BadgeType> earnedBadges = getEarnedBadges(doctorId);
        
        stats.setProgressTimelyProfessional(calculateTimelyProgress(stats, earnedBadges));
        stats.setProgressFlexibleCaregiver(calculateFlexibleProgress(stats, earnedBadges));
        stats.setProgressMedicalLegend(calculateLegendProgress(stats, earnedBadges));
        
        statisticsRepository.save(stats);
    }
    
    @Transactional
    public void updateAllBadgeProgress(UUID doctorId) {
        DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId).orElse(null);
        if (stats == null) return;
        
        List<BadgeType> earnedBadges = getEarnedBadges(doctorId);
        
        stats.setProgressExcellenceInCare(calculateExcellenceProgress(stats, earnedBadges));
        stats.setProgressEmpathyChampion(calculateEmpathyProgress(stats, earnedBadges));
        stats.setProgressClearCommunicator(calculateCommunicatorProgress(stats, earnedBadges));
        stats.setProgressDetailedDiagnostician(calculateDetailedDiagnostProgress(stats, earnedBadges));
        
        stats.setProgressTimelyProfessional(calculateTimelyProgress(stats, earnedBadges));
        stats.setProgressReliableExpert(calculateReliableProgress(stats, earnedBadges));
        stats.setProgressFlexibleCaregiver(calculateFlexibleProgress(stats, earnedBadges));
        stats.setProgressAgileResponder(calculateAgileProgress(stats, earnedBadges));
        
        stats.setProgressRelationshipBuilder(calculateRelationshipProgress(stats, earnedBadges));
        stats.setProgressTopSpecialist(calculateTopSpecialistProgress(stats, earnedBadges));
        stats.setProgressMedicalLegend(calculateLegendProgress(stats, earnedBadges));
        stats.setProgressAllStarDoctor(calculateAllStarProgress(stats, earnedBadges));
        
        statisticsRepository.save(stats);
    }
    
    private List<BadgeType> getEarnedBadges(UUID doctorId) {
        return badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId)
                .stream()
                .map(badge -> badge.getBadgeType())
                .toList();
    }
    
    private double calculateExcellenceProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.SUSTAINED_EXCELLENCE)) return 100.0;
        
        double avgRating = stats.getLast100AvgRating() != null ? stats.getLast100AvgRating() : 0.0;
        int totalRatings = stats.getTotalRatingsReceived();
        
        double ratingProgress = Math.min((avgRating / EXCELLENCE_RATING_THRESHOLD) * 100, 100);
        double countProgress = Math.min(((double) totalRatings / EXCELLENCE_MIN_RATINGS) * 100, 100);
        
        return Math.min((ratingProgress + countProgress) / 2, 100.0);
    }
    
    private double calculateEmpathyProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.EMPATHETIC_DOCTOR)) return 100.0;
        
        int empathyCount = stats.getLast50EmpathyCount();
        int totalRatings = stats.getTotalRatingsReceived();
        
        double empathyProgress = Math.min(((double) empathyCount / EMPATHY_COUNT_THRESHOLD) * 100, 100);
        double countProgress = Math.min(((double) totalRatings / EMPATHY_MIN_RATINGS) * 100, 100);
        
        return Math.min((empathyProgress + countProgress) / 2, 100.0);
    }
    
    private double calculateCommunicatorProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.EXCEPTIONAL_COMMUNICATOR)) return 100.0;
        
        int commCount = stats.getLast50CommunicationCount();
        int totalRatings = stats.getTotalRatingsReceived();
        
        double commProgress = Math.min(((double) commCount / COMMUNICATION_COUNT_THRESHOLD) * 100, 100);
        double countProgress = Math.min(((double) totalRatings / COMMUNICATION_MIN_RATINGS) * 100, 100);
        
        return Math.min((commProgress + countProgress) / 2, 100.0);
    }
    
    private double calculateDetailedDiagnostProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.DETAILED_HISTORIAN)) return 100.0;
        
        double avgWords = stats.getLast30AvgWordsPerEntry() != null ? stats.getLast30AvgWordsPerEntry() : 0.0;
        int documented = stats.getLast30DocumentedCount();
        
        double wordsProgress = Math.min((avgWords / DETAILED_AVG_WORDS_THRESHOLD) * 100, 100);
        double docProgress = Math.min(((double) documented / DETAILED_MIN_DOCUMENTED) * 100, 100);
        
        return Math.min((wordsProgress + docProgress) / 2, 100.0);
    }
    
    private double calculateTimelyProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.PUNCTUALITY_PROFESSIONAL)) return 100.0;
        
        int punctualityCount = stats.getLast50PunctualityCount();
        int totalRatings = stats.getTotalRatingsReceived();
        
        double punctProgress = Math.min(((double) punctualityCount / PUNCTUALITY_COUNT_THRESHOLD) * 100, 100);
        double countProgress = Math.min(((double) totalRatings / PUNCTUALITY_MIN_RATINGS) * 100, 100);
        
        return Math.min((punctProgress + countProgress) / 2, 100.0);
    }
    
    private double calculateReliableProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.COMPLETE_DOCUMENTER)) return 100.0;
        
        int documented = stats.getLast50DocumentedCount();
        int completed = Math.min(stats.getTotalTurnsCompleted(), 50);
        
        double documentationRate = completed > 0 ? ((double) documented / completed) * 100 : 0.0;
        
        return Math.min((documentationRate / DOCUMENTATION_RATE_THRESHOLD) * 100, 100);
    }
    
    private double calculateFlexibleProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.CONSISTENT_PROFESSIONAL)) return 100.0;
        
        int cancelled = stats.getTotalTurnsCancelled();
        int completed = stats.getTotalTurnsCompleted();
        int total = cancelled + completed;
        
        double cancellationRate = total > 0 ? ((double) cancelled / total) * 100 : 0.0;
        
        double cancellationProgress = cancellationRate <= CONSISTENT_CANCELLATION_RATE_THRESHOLD ? 100.0 : 
            Math.max(0, 100 - ((cancellationRate - CONSISTENT_CANCELLATION_RATE_THRESHOLD) * 5));
        
        double completedProgress = Math.min(((double) completed / CONSISTENT_MIN_COMPLETED) * 100, 100);
        
        return Math.min((cancellationProgress + completedProgress) / 2, 100.0);
    }
    
    private double calculateAgileProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.AGILE_RESPONDER)) return 100.0;
        
        int handled = stats.getLast10RequestsHandled();
        return Math.min(((double) handled / 5) * 100, 100.0);
    }
    
    private double calculateRelationshipProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.RELATIONSHIP_BUILDER)) return 100.0;
        
        int returning = stats.getReturningPatientsCount();
        int unique = stats.getTotalUniquePatients();
        
        double returningProgress = Math.min(((double) returning / RELATIONSHIP_RETURNING_THRESHOLD) * 100, 100);
        double uniqueProgress = Math.min(((double) unique / RELATIONSHIP_UNIQUE_THRESHOLD) * 100, 100);
        
        return Math.min((returningProgress + uniqueProgress) / 2, 100.0);
    }
    
    private double calculateTopSpecialistProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.TOP_SPECIALIST)) return 100.0;
        
        Double percentile = stats.getSpecialtyRankPercentile();
        int completed = stats.getTotalTurnsCompleted();
        
        double rankProgress = percentile != null && percentile <= TOP_SPECIALIST_PERCENTILE_THRESHOLD ? 100.0 : 
            (percentile != null ? Math.max(0, 100 - ((percentile - TOP_SPECIALIST_PERCENTILE_THRESHOLD) * 2)) : 0.0);
        
        double completedProgress = Math.min(((double) completed / TOP_SPECIALIST_MIN_COMPLETED) * 100, 100);
        
        return Math.min((rankProgress + completedProgress) / 2, 100.0);
    }
    
    private double calculateLegendProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.MEDICAL_LEGEND)) return 100.0;
        
        int completed = stats.getTotalTurnsCompleted();
        double avgRating = stats.getLast100AvgRating() != null ? stats.getLast100AvgRating() : 0.0;
        
        double completedProgress = Math.min(((double) completed / LEGEND_MIN_COMPLETED) * 100, 100);
        double ratingProgress = Math.min((avgRating / LEGEND_RATING_THRESHOLD) * 100, 100);
        
        return Math.min((completedProgress + ratingProgress) / 2, 100.0);
    }
    
    private double calculateAllStarProgress(DoctorBadgeStatistics stats, List<BadgeType> earnedBadges) {
        if (earnedBadges.contains(BadgeType.ALWAYS_AVAILABLE)) return 100.0;
        
        return 0.0;
    }
}
