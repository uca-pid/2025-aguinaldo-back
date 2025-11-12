package com.medibook.api.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "patient_badges", uniqueConstraints = {
    @UniqueConstraint(name = "uc_patient_badge_type", columnNames = {"patient_id", "badge_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientBadge {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false)
    private PatientBadgeType badgeType;

    @Column(name = "earned_at", nullable = false)
    private OffsetDateTime earnedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_evaluated_at", nullable = false)
    private OffsetDateTime lastEvaluatedAt;

    @PrePersist
    protected void onCreate() {
        if (earnedAt == null) {
            earnedAt = OffsetDateTime.now();
        }
        if (lastEvaluatedAt == null) {
            lastEvaluatedAt = OffsetDateTime.now();
        }
    }
}