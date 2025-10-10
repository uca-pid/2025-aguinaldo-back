package com.medibook.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateMedicalHistoryRequestDTO {
    @NotNull
    private UUID patientId;

    @NotNull
    private UUID turnId;

    @Size(max = 5000, message = "Medical history must be less than 5000 characters")
    private String medicalHistory; 
}