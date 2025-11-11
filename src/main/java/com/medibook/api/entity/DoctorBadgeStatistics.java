package com.medibook.api.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "doctor_badge_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorBadgeStatistics {

    @Id
    @Column(name = "doctor_id")
    private UUID doctorId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "doctor_id")
    private User doctor;
    
    @Column(name = "total_ratings_received", nullable = false)
    @Builder.Default
    private Integer totalRatingsReceived = 0;

    @Column(name = "last_50_communication_count", nullable = false)
    @Builder.Default
    private Integer last50CommunicationCount = 0;

    @Column(name = "last_50_empathy_count", nullable = false)
    @Builder.Default
    private Integer last50EmpathyCount = 0;

    @Column(name = "last_50_punctuality_count", nullable = false)
    @Builder.Default
    private Integer last50PunctualityCount = 0;

    @Column(name = "last_100_avg_rating")
    private Double last100AvgRating;

    @Column(name = "last_100_low_rating_count", nullable = false)
    @Builder.Default
    private Integer last100LowRatingCount = 0;
    
    @Column(name = "total_turns_completed", nullable = false)
    @Builder.Default
    private Integer totalTurnsCompleted = 0;

    @Column(name = "total_turns_cancelled", nullable = false)
    @Builder.Default
    private Integer totalTurnsCancelled = 0;

    @Column(name = "total_turns_no_show", nullable = false)
    @Builder.Default
    private Integer totalTurnsNoShow = 0;

    @Column(name = "turns_last_90_days", nullable = false)
    @Builder.Default
    private Integer turnsLast90Days = 0;

    @Column(name = "cancellations_last_90_days", nullable = false)
    @Builder.Default
    private Integer cancellationsLast90Days = 0;

    @Column(name = "no_shows_last_90_days", nullable = false)
    @Builder.Default
    private Integer noShowsLast90Days = 0;
    
    @Column(name = "last_50_documented_count", nullable = false)
    @Builder.Default
    private Integer last50DocumentedCount = 0;

    @Column(name = "last_30_documented_count", nullable = false)
    @Builder.Default
    private Integer last30DocumentedCount = 0;

    @Column(name = "last_30_total_words", nullable = false)
    @Builder.Default
    private Integer last30TotalWords = 0;

    @Column(name = "last_30_avg_words_per_entry")
    @Builder.Default
    private Double last30AvgWordsPerEntry = 0.0;
    
    @Column(name = "total_unique_patients", nullable = false)
    @Builder.Default
    private Integer totalUniquePatients = 0;

    @Column(name = "returning_patients_count", nullable = false)
    @Builder.Default
    private Integer returningPatientsCount = 0;
    
    @Column(name = "last_10_requests_handled", nullable = false)
    @Builder.Default
    private Integer last10RequestsHandled = 0;
    
    @Column(name = "specialty_rank_percentile")
    private Double specialtyRankPercentile;

    @Column(name = "progress_excellence_in_care")
    @Builder.Default
    private Double progressExcellenceInCare = 0.0;

    @Column(name = "progress_empathy_champion")
    @Builder.Default
    private Double progressEmpathyChampion = 0.0;

    @Column(name = "progress_clear_communicator")
    @Builder.Default
    private Double progressClearCommunicator = 0.0;

    @Column(name = "progress_detailed_diagnostician")
    @Builder.Default
    private Double progressDetailedDiagnostician = 0.0;

    @Column(name = "progress_timely_professional")
    @Builder.Default
    private Double progressTimelyProfessional = 0.0;

    @Column(name = "progress_reliable_expert")
    @Builder.Default
    private Double progressReliableExpert = 0.0;

    @Column(name = "progress_flexible_caregiver")
    @Builder.Default
    private Double progressFlexibleCaregiver = 0.0;

    @Column(name = "progress_agile_responder")
    @Builder.Default
    private Double progressAgileResponder = 0.0;

    @Column(name = "progress_relationship_builder")
    @Builder.Default
    private Double progressRelationshipBuilder = 0.0;

    @Column(name = "progress_top_specialist")
    @Builder.Default
    private Double progressTopSpecialist = 0.0;

    @Column(name = "progress_medical_legend")
    @Builder.Default
    private Double progressMedicalLegend = 0.0;

    @Column(name = "progress_all_star_doctor")
    @Builder.Default
    private Double progressAllStarDoctor = 0.0;
    
    @Column(name = "last_updated_at", nullable = false)
    private OffsetDateTime lastUpdatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = OffsetDateTime.now();
    }
}
