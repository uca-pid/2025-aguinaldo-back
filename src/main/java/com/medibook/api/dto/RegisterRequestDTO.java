package com.medibook.api.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegisterRequestDTO(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password,

    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Surname is required")
    String surname,

    String phone,
    LocalDate birthdate,
    String gender,


    String medicalLicense,
    String specialty,

    @Min(value = 1, message = "Slot duration must be positive")
    Integer slotDurationMin
) {}