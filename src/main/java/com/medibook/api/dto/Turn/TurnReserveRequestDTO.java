package com.medibook.api.dto.Turn;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class TurnReserveRequestDTO {
    private UUID turnId;
    private UUID patientId;
}
