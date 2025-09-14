package com.medibook.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class TurnResponseDTO {
    private UUID id;
    private UUID doctorId;
    private String doctorName;
    private UUID patientId;     
    private String patientName;
    private OffsetDateTime scheduledAt;
    private String status;
}
