package com.medibook.api.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ProfileResponseDTO {
    private UUID id;
    private String email;
    private String name;
    private String surname;
    private Long dni;
    private String phone;
    private LocalDate birthdate;
    private String gender;
    private String role;
    private String status;

    // Doctor-specific fields (will be null for non-doctors)
    private String medicalLicense;
    private String specialty;
    private Integer slotDurationMin;
}
