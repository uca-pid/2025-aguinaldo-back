package com.medibook.api.dto.Availability;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityResponseDTO {
    
    private Integer slotDurationMin;
    private List<DayAvailabilityDTO> weeklyAvailability;
}