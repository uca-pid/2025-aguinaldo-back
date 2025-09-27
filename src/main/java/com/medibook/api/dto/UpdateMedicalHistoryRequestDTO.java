package com.medibook.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateMedicalHistoryRequestDTO {
    @NotNull
    private UUID patientId;
    
    private String medicalHistory; // Can be null or empty to clear the history
}