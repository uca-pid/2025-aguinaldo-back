package com.medibook.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "badges", uniqueConstraints = {
    @UniqueConstraint(name = "uc_user_badge_type", columnNames = {"user_id", "badge_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private User user;

    @Column(name = "badge_type", nullable = false)
    private String badgeType;

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