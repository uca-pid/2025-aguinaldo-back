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
    private UUID patientId;     // null si está libre
    private String patientName; // null si está libre
    private OffsetDateTime scheduledAt;
    private String status;
}
