package com.medibook.api.dto.Registration;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReviewDoctorRegistrationRequestDTO(
    @NotNull(message = "Registration ID is required")
    UUID registrationId,
    
    @NotNull(message = "Approval status is required")
    Boolean approved,
    
    String rejectionReason
) {
    public ReviewDoctorRegistrationRequestDTO {
        if (!approved && (rejectionReason == null || rejectionReason.trim().isEmpty())) {
            throw new IllegalArgumentException("Rejection reason is required when rejecting a registration");
        }
    }
}