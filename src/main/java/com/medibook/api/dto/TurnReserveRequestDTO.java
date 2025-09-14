package com.medibook.api.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class TurnReserveRequestDTO {
    private UUID turnId;  // El turno que el usuario quiere reservar
    private UUID patientId;
}
