package com.medibook.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMedicalHistoryContentRequestDTO {
    @Size(max = 5000, message = "Medical history content must be less than 5000 characters")
    private String content;
}