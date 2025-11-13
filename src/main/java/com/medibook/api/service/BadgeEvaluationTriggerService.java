package com.medibook.api.service;

import com.medibook.api.entity.User;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeEvaluationTriggerService {

    private final DoctorBadgeService badgeService;
    private final DoctorBadgeStatisticsUpdateService statisticsUpdateService;
    private final UserRepository userRepository;

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterRating(UUID doctorId, Integer communicationScore, Integer empathyScore, Integer punctualityScore) {
        try {
            validateDoctorRole(doctorId);
            
            statisticsUpdateService.updateAfterRatingAddedSync(doctorId, communicationScore, empathyScore, punctualityScore);
            statisticsUpdateService.updateProgressAfterRatingSync(doctorId);
            
            badgeService.evaluateRatingRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating rating-related badges for doctor {} after rating: {}", doctorId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterTurnCompletion(UUID doctorId, UUID patientId) {
        try {
            validateDoctorRole(doctorId);
            
            statisticsUpdateService.updateAfterTurnCompletedSync(doctorId, patientId);
            statisticsUpdateService.updateProgressAfterTurnCompletionSync(doctorId);
            
            badgeService.evaluateTurnCompletionRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating turn completion badges for doctor {} after turn completion: {}", doctorId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterMedicalHistoryDocumented(UUID doctorId, String content) {
        try {
            validateDoctorRole(doctorId);
            
            statisticsUpdateService.updateAfterMedicalHistoryDocumentedSync(doctorId, content);
            statisticsUpdateService.updateProgressAfterMedicalHistorySync(doctorId);
            
            badgeService.evaluateDocumentationRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating documentation badges for doctor {} after medical history: {}", doctorId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterModifyRequestHandled(UUID doctorId) {
        try {
            validateDoctorRole(doctorId);
            
            statisticsUpdateService.updateAfterModifyRequestHandledSync(doctorId);
            statisticsUpdateService.updateProgressAfterModifyRequestSync(doctorId);
            
            badgeService.evaluateResponseRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating response badges for doctor {} after modify request: {}", doctorId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterTurnCancellation(UUID doctorId) {
        try {
            validateDoctorRole(doctorId);
            
            statisticsUpdateService.updateAfterTurnCancelledSync(doctorId);
            statisticsUpdateService.updateProgressAfterCancellationSync(doctorId);
            
            badgeService.evaluateConsistencyRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating consistency badges for doctor {} after cancellation: {}", doctorId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterTurnNoShow(UUID doctorId) {
        try {
            validateDoctorRole(doctorId);
            
            statisticsUpdateService.updateAfterTurnNoShowSync(doctorId);
            statisticsUpdateService.updateProgressAfterCancellationSync(doctorId);
            
            badgeService.evaluateConsistencyRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating consistency badges for doctor {} after no-show: {}", doctorId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterAvailabilityConfigured(UUID doctorId) {
        try {
            validateDoctorRole(doctorId);
            badgeService.evaluateAlwaysAvailableBadge(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating ALWAYS_AVAILABLE badge for doctor {} after availability configuration: {}", doctorId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAllBadges(UUID doctorId) {
        try {
            validateDoctorRole(doctorId);            
            statisticsUpdateService.updateAllBadgeProgress(doctorId);
            badgeService.evaluateAllBadges(doctorId);
            
        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating all badges for doctor {}: {}", doctorId, e.getMessage(), e);
        }
    }
    
    private void validateDoctorRole(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        if (!"DOCTOR".equals(user.getRole())) {
            log.warn("Attempted badge evaluation for non-doctor user: {} (role: {})", userId, user.getRole());
            throw new IllegalArgumentException("Badge evaluation only allowed for doctors");
        }
    }
}
