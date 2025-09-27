package com.medibook.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "turn_modify_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnModifyRequest {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_assigned_id", nullable = false)
    private TurnAssigned turnAssigned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Column(name = "current_scheduled_at", nullable = false)
    private OffsetDateTime currentScheduledAt;

    @Column(name = "requested_scheduled_at", nullable = false)
    private OffsetDateTime requestedScheduledAt;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, APPROVED, REJECTED
}