package com.medibook.api.dto.Availability;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityRequestDTO {
    
    @Valid
    @NotNull(message = "Weekly availability is required")
    private List<DayAvailabilityDTO> weeklyAvailability;
}