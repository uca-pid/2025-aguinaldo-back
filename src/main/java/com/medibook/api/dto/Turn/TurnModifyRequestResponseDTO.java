package com.medibook.api.dto.Turn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnModifyRequestResponseDTO {
    
    private UUID id;
    private UUID turnId;
    private UUID patientId;
    private UUID doctorId;
    private OffsetDateTime currentScheduledAt;
    private OffsetDateTime requestedScheduledAt;
    private String status;
}