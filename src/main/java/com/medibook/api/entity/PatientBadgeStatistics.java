package com.medibook.api.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "patient_badge_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientBadgeStatistics {

    @Id
    @Column(name = "patient_id")
    private UUID patientId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "patient_id")
    private User patient;

    @Column(name = "total_turns_completed", nullable = false)
    @Builder.Default
    private Integer totalTurnsCompleted = 0;

    @Column(name = "total_turns_cancelled", nullable = false)
    @Builder.Default
    private Integer totalTurnsCancelled = 0;

    @Column(name = "total_turns_no_show", nullable = false)
    @Builder.Default
    private Integer totalTurnsNoShow = 0;

    @Column(name = "turns_last_12_months", nullable = false)
    @Builder.Default
    private Integer turnsLast12Months = 0;

    @Column(name = "turns_last_6_months", nullable = false)
    @Builder.Default
    private Integer turnsLast6Months = 0;

    @Column(name = "turns_last_90_days", nullable = false)
    @Builder.Default
    private Integer turnsLast90Days = 0;

    @Column(name = "last_5_turns_attendance_rate")
    private Double last5TurnsAttendanceRate;

    @Column(name = "last_10_turns_punctual_count", nullable = false)
    @Builder.Default
    private Integer last10TurnsPunctualCount = 0;

    @Column(name = "last_5_turns_advance_booking_count", nullable = false)
    @Builder.Default
    private Integer last5TurnsAdvanceBookingCount = 0;

    @Column(name = "last_15_turns_collaboration_count", nullable = false)
    @Builder.Default
    private Integer last15TurnsCollaborationCount = 0;

    @Column(name = "last_15_turns_follow_instructions_count", nullable = false)
    @Builder.Default
    private Integer last15TurnsFollowInstructionsCount = 0;

    @Column(name = "last_10_turns_files_uploaded_count", nullable = false)
    @Builder.Default
    private Integer last10TurnsFilesUploadedCount = 0;

    @Column(name = "total_ratings_given", nullable = false)
    @Builder.Default
    private Integer totalRatingsGiven = 0;

    @Column(name = "total_ratings_received", nullable = false)
    @Builder.Default
    private Integer totalRatingsReceived = 0;

    @Column(name = "avg_rating_given")
    private Double avgRatingGiven;

    @Column(name = "avg_rating_received")
    private Double avgRatingReceived;

    @Column(name = "total_unique_doctors", nullable = false)
    @Builder.Default
    private Integer totalUniqueDoctors = 0;

    @Column(name = "turns_with_same_doctor_last_12_months", nullable = false)
    @Builder.Default
    private Integer turnsWithSameDoctorLast12Months = 0;

    @Column(name = "different_specialties_last_12_months", nullable = false)
    @Builder.Default
    private Integer differentSpecialtiesLast12Months = 0;

    // Progress fields for each badge
    @Column(name = "progress_medi_book_welcome")
    @Builder.Default
    private Double progressMediBookWelcome = 0.0;

    @Column(name = "progress_health_guardian")
    @Builder.Default
    private Double progressHealthGuardian = 0.0;

    @Column(name = "progress_committed_patient")
    @Builder.Default
    private Double progressCommittedPatient = 0.0;

    @Column(name = "progress_continuous_followup")
    @Builder.Default
    private Double progressContinuousFollowup = 0.0;

    @Column(name = "progress_constant_patient")
    @Builder.Default
    private Double progressConstantPatient = 0.0;

    @Column(name = "progress_exemplary_punctuality")
    @Builder.Default
    private Double progressExemplaryPunctuality = 0.0;

    @Column(name = "progress_smart_planner")
    @Builder.Default
    private Double progressSmartPlanner = 0.0;

    @Column(name = "progress_excellent_collaborator")
    @Builder.Default
    private Double progressExcellentCollaborator = 0.0;

    @Column(name = "progress_always_prepared")
    @Builder.Default
    private Double progressAlwaysPrepared = 0.0;

    @Column(name = "progress_responsible_evaluator")
    @Builder.Default
    private Double progressResponsibleEvaluator = 0.0;

    @Column(name = "progress_excellence_model")
    @Builder.Default
    private Double progressExcellenceModel = 0.0;

    @Column(name = "last_updated_at", nullable = false)
    private OffsetDateTime lastUpdatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = OffsetDateTime.now();
    }
}