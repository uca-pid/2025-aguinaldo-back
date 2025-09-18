package com.medibook.api.dto.Turn;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TurnCreateRequestDTO {
    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;
    
    @NotNull(message = "Patient ID is required") 
    private UUID patientId;
    
    @NotNull(message = "Scheduled time is required")
    private OffsetDateTime scheduledAt;
}
