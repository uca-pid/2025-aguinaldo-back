package com.medibook.api.dto.Registration;

import java.time.ZonedDateTime;
import java.util.UUID;

public record PendingDoctorRegistrationResponseDTO(
    UUID id,
    String email,
    Long dni,
    String name,
    String surname,
    String phone,
    String birthdate,
    String gender,
    String medicalLicense,
    String specialty,
    Integer slotDurationMin,
    String status,
    ZonedDateTime createdAt,
    ZonedDateTime reviewedAt,
    UUID reviewedBy,
    String rejectionReason
) {}