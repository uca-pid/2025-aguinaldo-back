package com.medibook.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class DoctorDTO {
    private UUID id;
    private String name;
    private String surname;
    private String email;
    private String medicalLicense;
    private String specialty;
    private int slotDurationMin;
    private Double score;
}
