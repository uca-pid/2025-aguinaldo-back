package com.medibook.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JsonBackReference
    private User user;

    @Column(name = "medical_license", nullable = false, unique = true)
    private String medicalLicense;

    @Column(nullable = false)
    private String specialty;

    @Column(name = "slot_duration_min", nullable = false)
    private int slotDurationMin = 15;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "availability_schedule", columnDefinition = "json")
    private String availabilitySchedule;
}
