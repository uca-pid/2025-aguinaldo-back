package com.medibook.api.dto.Turn;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TurnCreateRequestDTO {
    private UUID doctorId;
     private UUID patientId;
    private OffsetDateTime scheduledAt;
}
