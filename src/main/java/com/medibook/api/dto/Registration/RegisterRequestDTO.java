package com.medibook.api.dto.Registration;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegisterRequestDTO(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotNull(message = "DNI is required")
    @Min(value = 1000000L, message = "DNI must be at least 7 digits")
    @Max(value = 999999999L, message = "DNI must be at most 9 digits")
    Long dni,

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