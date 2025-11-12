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
public class PatientBadgeEvaluationTriggerService {

    private final PatientBadgeService badgeService;
    private final PatientBadgeStatisticsUpdateService statisticsUpdateService;
    private final UserRepository userRepository;

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterTurnCompletion(UUID patientId, UUID doctorId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAfterTurnCompletedSync(patientId, doctorId);
            statisticsUpdateService.updateProgressAfterTurnCompletionSync(patientId);
            badgeService.evaluateTurnRelatedBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating turn completion badges for patient {} after turn completion: {}", patientId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterTurnCancellation(UUID patientId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAfterTurnCancelledSync(patientId);
            statisticsUpdateService.updateProgressAfterTurnCompletionSync(patientId);
            badgeService.evaluateTurnRelatedBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating turn cancellation badges for patient {} after cancellation: {}", patientId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterTurnNoShow(UUID patientId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAfterTurnNoShowSync(patientId);
            statisticsUpdateService.updateProgressAfterTurnCompletionSync(patientId);
            badgeService.evaluateTurnRelatedBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating turn no-show badges for patient {} after no-show: {}", patientId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterRatingGiven(UUID patientId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAfterRatingGivenSync(patientId);
            statisticsUpdateService.updateProgressAfterRatingSync(patientId);
            badgeService.evaluateRatingRelatedBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating rating badges for patient {} after rating given: {}", patientId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterRatingReceived(UUID patientId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAfterRatingReceivedSync(patientId);
            statisticsUpdateService.updateProgressAfterRatingSync(patientId);
            badgeService.evaluateRatingRelatedBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating rating received badges for patient {} after rating received: {}", patientId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterFileUploaded(UUID patientId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAfterFileUploadedSync(patientId);
            statisticsUpdateService.updateProgressAfterFileUploadSync(patientId);
            badgeService.evaluateFileRelatedBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating file upload badges for patient {} after file upload: {}", patientId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterAdvanceBooking(UUID patientId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAfterAdvanceBookingSync(patientId);
            statisticsUpdateService.updateProgressAfterBookingSync(patientId);
            badgeService.evaluateBookingRelatedBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating advance booking badges for patient {} after booking: {}", patientId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterPunctualityRating(UUID patientId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAfterPunctualityRatingSync(patientId);
            statisticsUpdateService.updateProgressAfterRatingSync(patientId);
            badgeService.evaluateRatingRelatedBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating punctuality rating badges for patient {} after rating: {}", patientId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterCollaborationRating(UUID patientId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAfterCollaborationRatingSync(patientId);
            statisticsUpdateService.updateProgressAfterRatingSync(patientId);
            badgeService.evaluateRatingRelatedBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating collaboration rating badges for patient {} after rating: {}", patientId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAfterFollowInstructionsRating(UUID patientId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAfterFollowInstructionsRatingSync(patientId);
            statisticsUpdateService.updateProgressAfterRatingSync(patientId);
            badgeService.evaluateRatingRelatedBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating follow instructions rating badges for patient {} after rating: {}", patientId, e.getMessage(), e);
        }
    }

    @Async("badgeEvaluationTaskExecutor")
    public void evaluateAllBadges(UUID patientId) {
        try {
            validatePatientRole(patientId);
            statisticsUpdateService.updateAllBadgeProgress(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("[TRIGGER] Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[TRIGGER] Unexpected error evaluating all badges for patient {}: {}", patientId, e.getMessage(), e);
        }
    }

    private void validatePatientRole(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!"PATIENT".equals(user.getRole())) {
            log.warn("Attempted badge evaluation for non-patient user: {} (role: {})", userId, user.getRole());
            throw new IllegalArgumentException("Badge evaluation only allowed for patients");
        }
    }
}