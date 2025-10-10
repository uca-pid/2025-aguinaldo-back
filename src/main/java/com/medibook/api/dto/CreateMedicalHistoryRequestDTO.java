package com.medibook.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * DTO for creating medical history entries associated with turns
 */
@Data
public class CreateMedicalHistoryRequestDTO {
    @NotNull(message = "Turn ID is required")
    private UUID turnId;

    @Size(max = 5000, message = "Medical history content must be less than 5000 characters")
    private String content;
}