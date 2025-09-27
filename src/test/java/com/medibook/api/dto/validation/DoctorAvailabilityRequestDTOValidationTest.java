package com.medibook.api.dto.validation;

import com.medibook.api.dto.Availability.DayAvailabilityDTO;
import com.medibook.api.dto.Availability.DoctorAvailabilityRequestDTO;
import com.medibook.api.dto.Availability.TimeRangeDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DoctorAvailabilityRequestDTOValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validDoctorAvailabilityRequest_ShouldPass() {        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", true, List.of(
                new TimeRangeDTO("09:00", "12:00"),
                new TimeRangeDTO("14:00", "18:00")
            )),
            new DayAvailabilityDTO("TUESDAY", true, List.of(
                new TimeRangeDTO("08:00", "16:00")
            )),
            new DayAvailabilityDTO("WEDNESDAY", false, Collections.emptyList()),
            new DayAvailabilityDTO("THURSDAY", true, List.of(
                new TimeRangeDTO("10:00", "15:00")
            )),
            new DayAvailabilityDTO("FRIDAY", true, List.of(
                new TimeRangeDTO("09:00", "17:00")
            )),
            new DayAvailabilityDTO("SATURDAY", false, Collections.emptyList()),
            new DayAvailabilityDTO("SUNDAY", false, Collections.emptyList())
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(30, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void validMinimalSlotDuration_ShouldPass() {
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", false, Collections.emptyList())
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(5, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void validLargeSlotDuration_ShouldPass() {
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", false, Collections.emptyList())
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(120, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullSlotDuration_ShouldFail() {
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", false, Collections.emptyList())
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(null, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Slot duration is required")));
    }

    @Test
    void slotDurationTooSmall_ShouldFail() {
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", false, Collections.emptyList())
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(4, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Slot duration must be at least 5 minutes")));
    }

    @Test
    void slotDurationZero_ShouldFail() {
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", false, Collections.emptyList())
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(0, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Slot duration must be at least 5 minutes")));
    }

    @Test
    void slotDurationNegative_ShouldFail() {
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", false, Collections.emptyList())
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(-10, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Slot duration must be at least 5 minutes")));
    }

    @Test
    void nullWeeklyAvailability_ShouldFail() {
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(30, null);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Weekly availability is required")));
    }

    @Test
    void emptyWeeklyAvailability_ShouldPass() {
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(30, Collections.emptyList());
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidDayInWeeklyAvailability_ShouldFail() {        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", true, List.of(
                new TimeRangeDTO("09:00", "17:00")
            )),
            new DayAvailabilityDTO("", false, Collections.emptyList()),
            new DayAvailabilityDTO("WEDNESDAY", null, Collections.emptyList())
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(30, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Day is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Enabled status is required")));
    }

    @Test
    void invalidTimeRangeInWeeklyAvailability_ShouldFail() {        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", true, List.of(
                new TimeRangeDTO("25:00", "17:00"),
                new TimeRangeDTO("09:00", "24:60")
            ))
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(30, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time must be in HH:MM format")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("End time must be in HH:MM format")));
    }

    @Test
    void multipleViolations_ShouldReturnAllViolations() {
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("", null, List.of(
                new TimeRangeDTO("", "25:00")
            ))
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(-5, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 4);
        
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Slot duration must be at least 5 minutes")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Day is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Enabled status is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("End time must be in HH:MM format")));
    }

    @Test
    void complexValidAvailability_ShouldPass() {
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", true, List.of(
                new TimeRangeDTO("08:30", "12:30"),
                new TimeRangeDTO("13:30", "17:30"),
                new TimeRangeDTO("18:00", "20:00")
            )),
            new DayAvailabilityDTO("TUESDAY", true, List.of(
                new TimeRangeDTO("00:00", "23:59")
            )),
            new DayAvailabilityDTO("WEDNESDAY", false, Collections.emptyList()),
            new DayAvailabilityDTO("THURSDAY", true, List.of(
                new TimeRangeDTO("7:00", "8:00")
            )),
            new DayAvailabilityDTO("FRIDAY", true, List.of(
                new TimeRangeDTO("09:00", "09:30")
            )),
            new DayAvailabilityDTO("SATURDAY", false, Collections.emptyList()),
            new DayAvailabilityDTO("SUNDAY", false, Collections.emptyList())
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(15, weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty());
    }
}