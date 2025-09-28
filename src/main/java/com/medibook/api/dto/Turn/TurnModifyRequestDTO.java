package com.medibook.api.dto.Turn;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnModifyRequestDTO {
    
    @NotNull(message = "Turn ID is required")
    private UUID turnId;
    
    @NotNull(message = "New scheduled time is required")
    private OffsetDateTime newScheduledAt;
}