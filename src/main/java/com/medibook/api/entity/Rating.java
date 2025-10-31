package com.medibook.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"turn_id", "rater_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_id", nullable = false)
    private TurnAssigned turnAssigned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rater_id", nullable = false)
    private User rater;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_id", nullable = false)
    private User rated;

    @Column(name = "score", nullable = false)
    private Integer score; 

    @Column(name = "subcategory")
    private String subcategory; 

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
