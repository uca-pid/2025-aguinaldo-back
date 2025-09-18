package com.medibook.api.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ProfileUpdateRequestDTO(
    @Email(message = "Invalid email format")
    String email,

    @Size(min = 1, message = "Name cannot be empty")
    String name,

    @Size(min = 1, message = "Surname cannot be empty") 
    String surname,

    String phone,
    LocalDate birthdate,
    String gender,

    // Doctor-specific fields (only used if user is a doctor)
    String medicalLicense,
    String specialty,

    @Min(value = 1, message = "Slot duration must be positive")
    Integer slotDurationMin
) {}