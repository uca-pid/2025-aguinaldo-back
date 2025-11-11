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

    @Async
    public void evaluateAfterRating(UUID doctorId, Integer communicationScore, Integer empathyScore, Integer punctualityScore) {
        try {
            validateDoctorRole(doctorId);            
            log.info("Updating statistics and triggering rating-related badge evaluation for doctor {}", doctorId);
            statisticsUpdateService.updateAfterRatingAdded(doctorId, communicationScore, empathyScore, punctualityScore);
            statisticsUpdateService.updateProgressAfterRating(doctorId);            
            badgeService.evaluateRatingRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating rating-related badges for doctor {} after rating: {}", doctorId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterTurnCompletion(UUID doctorId, UUID patientId) {
        try {
            validateDoctorRole(doctorId);            
            log.info("Updating statistics and triggering turn completion badge evaluation for doctor {}", doctorId);
            statisticsUpdateService.updateAfterTurnCompleted(doctorId, patientId);
            statisticsUpdateService.updateProgressAfterTurnCompletion(doctorId);
            badgeService.evaluateTurnCompletionRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating turn completion badges for doctor {} after turn completion: {}", doctorId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterMedicalHistoryDocumented(UUID doctorId, String content) {
        try {
            validateDoctorRole(doctorId);            
            log.info("Updating statistics and triggering documentation-related badge evaluation for doctor {}", doctorId);            
            statisticsUpdateService.updateAfterMedicalHistoryDocumented(doctorId, content);            
            statisticsUpdateService.updateProgressAfterMedicalHistory(doctorId);            
            badgeService.evaluateDocumentationRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating documentation badges for doctor {} after medical history: {}", doctorId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterModifyRequestHandled(UUID doctorId) {
        try {
            validateDoctorRole(doctorId);            
            log.info("Updating statistics and triggering response-related badge evaluation for doctor {}", doctorId);            
            statisticsUpdateService.updateAfterModifyRequestHandled(doctorId);            
            statisticsUpdateService.updateProgressAfterModifyRequest(doctorId);            
            badgeService.evaluateResponseRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating response badges for doctor {} after modify request: {}", doctorId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterTurnCancellation(UUID doctorId) {
        try {
            validateDoctorRole(doctorId);            
            log.info("Updating statistics and triggering consistency-related badge evaluation for doctor {}", doctorId);            
            statisticsUpdateService.updateAfterTurnCancelled(doctorId);            
            statisticsUpdateService.updateProgressAfterCancellation(doctorId);            
            badgeService.evaluateConsistencyRelatedBadges(doctorId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating consistency badges for doctor {} after cancellation: {}", doctorId, e.getMessage());
        }
    }

    @Async
    public void evaluateAllBadges(UUID doctorId) {
        try {
            validateDoctorRole(doctorId);            
            log.info("Triggering FULL badge evaluation for doctor {} (all badges)", doctorId);            
            statisticsUpdateService.updateAllBadgeProgress(doctorId);
            badgeService.evaluateAllBadges(doctorId);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid doctor role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating all badges for doctor {}: {}", doctorId, e.getMessage());
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
