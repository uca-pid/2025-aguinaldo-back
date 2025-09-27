package com.medibook.api.dto.Availability;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayAvailabilityDTO {
    
    @NotBlank(message = "Day is required")
    @Pattern(regexp = "^(?i)(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)$", 
             message = "Day must be one of: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY")
    private String day;  // "MONDAY", "TUESDAY", etc.
    
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
    
    @Valid
    private List<TimeRangeDTO> ranges;
}