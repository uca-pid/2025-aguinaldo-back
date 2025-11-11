package com.medibook.api.dto.Availability;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotDTO {
    
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String dayOfWeek;
    private Boolean isOccupied; // null if not checked, true if occupied, false if available
}