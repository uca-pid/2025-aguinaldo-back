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

    @Async
    public void evaluateAfterTurnCompletion(UUID patientId, UUID doctorId) {
        try {
            validatePatientRole(patientId);
            log.info("Updating statistics and triggering turn completion badge evaluation for patient {}", patientId);
            statisticsUpdateService.updateAfterTurnCompleted(patientId, doctorId);
            statisticsUpdateService.updateProgressAfterTurnCompletion(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating turn completion badges for patient {} after turn completion: {}", patientId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterTurnCancellation(UUID patientId) {
        try {
            validatePatientRole(patientId);
            log.info("Updating statistics and triggering consistency-related badge evaluation for patient {}", patientId);
            statisticsUpdateService.updateAfterTurnCancelled(patientId);
            statisticsUpdateService.updateProgressAfterTurnCompletion(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating consistency badges for patient {} after cancellation: {}", patientId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterTurnNoShow(UUID patientId) {
        try {
            validatePatientRole(patientId);
            log.info("Updating statistics and triggering no-show badge evaluation for patient {}", patientId);
            statisticsUpdateService.updateAfterTurnNoShow(patientId);
            statisticsUpdateService.updateProgressAfterTurnCompletion(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating no-show badges for patient {} after no-show: {}", patientId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterRatingGiven(UUID patientId) {
        try {
            validatePatientRole(patientId);
            log.info("Updating statistics and triggering rating-related badge evaluation for patient {}", patientId);
            statisticsUpdateService.updateAfterRatingGiven(patientId);
            statisticsUpdateService.updateProgressAfterRating(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating rating badges for patient {} after rating given: {}", patientId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterRatingReceived(UUID patientId) {
        try {
            validatePatientRole(patientId);
            log.info("Updating statistics and triggering rating received badge evaluation for patient {}", patientId);
            statisticsUpdateService.updateAfterRatingReceived(patientId);
            statisticsUpdateService.updateProgressAfterRating(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating rating received badges for patient {} after rating received: {}", patientId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterFileUploaded(UUID patientId) {
        try {
            validatePatientRole(patientId);
            log.info("Updating statistics and triggering file upload badge evaluation for patient {}", patientId);
            statisticsUpdateService.updateAfterFileUploaded(patientId);
            statisticsUpdateService.updateProgressAfterFileUpload(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating file upload badges for patient {} after file upload: {}", patientId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterAdvanceBooking(UUID patientId) {
        try {
            validatePatientRole(patientId);
            log.info("Updating statistics and triggering advance booking badge evaluation for patient {}", patientId);
            statisticsUpdateService.updateAfterAdvanceBooking(patientId);
            statisticsUpdateService.updateProgressAfterBooking(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating advance booking badges for patient {} after booking: {}", patientId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterPunctualityRating(UUID patientId) {
        try {
            validatePatientRole(patientId);
            log.info("Updating statistics and triggering punctuality rating badge evaluation for patient {}", patientId);
            statisticsUpdateService.updateAfterPunctualityRating(patientId);
            statisticsUpdateService.updateProgressAfterRating(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating punctuality rating badges for patient {} after rating: {}", patientId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterCollaborationRating(UUID patientId) {
        try {
            validatePatientRole(patientId);
            log.info("Updating statistics and triggering collaboration rating badge evaluation for patient {}", patientId);
            statisticsUpdateService.updateAfterCollaborationRating(patientId);
            statisticsUpdateService.updateProgressAfterRating(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating collaboration rating badges for patient {} after rating: {}", patientId, e.getMessage());
        }
    }

    @Async
    public void evaluateAfterFollowInstructionsRating(UUID patientId) {
        try {
            validatePatientRole(patientId);
            log.info("Updating statistics and triggering follow instructions rating badge evaluation for patient {}", patientId);
            statisticsUpdateService.updateAfterFollowInstructionsRating(patientId);
            statisticsUpdateService.updateProgressAfterRating(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating follow instructions rating badges for patient {} after rating: {}", patientId, e.getMessage());
        }
    }

    @Async
    public void evaluateAllBadges(UUID patientId) {
        try {
            validatePatientRole(patientId);
            log.info("Triggering FULL badge evaluation for patient {} (all badges)", patientId);
            statisticsUpdateService.updateAllBadgeProgress(patientId);
            badgeService.evaluateAllBadges(patientId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid patient role for badge evaluation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating all badges for patient {}: {}", patientId, e.getMessage());
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