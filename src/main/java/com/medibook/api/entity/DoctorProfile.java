package com.medibook.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "doctor_profiles")
public class DoctorProfile {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "medical_license", nullable = false, unique = true)
    private String medicalLicense;

    @Column(nullable = false)
    private String specialty;

    @Column(name = "slot_duration_min", nullable = false)
    private int slotDurationMin = 15;

    @Column(name = "reviewed_at")
    private ZonedDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}
