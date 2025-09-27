package com.medibook.api.dto.validation;

import com.medibook.api.dto.Availability.DayAvailabilityDTO;
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

class DayAvailabilityDTOValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validDayAvailability_ShouldPass() {
        List<TimeRangeDTO> timeRanges = List.of(
            new TimeRangeDTO("09:00", "12:00"),
            new TimeRangeDTO("14:00", "18:00")
        );
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("MONDAY", true, timeRanges);
        
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void validDayAvailability_EmptyRanges_ShouldPass() {
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("SUNDAY", false, Collections.emptyList());
        
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullDay_ShouldFail() {
        List<TimeRangeDTO> timeRanges = List.of(new TimeRangeDTO("09:00", "17:00"));
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO(null, true, timeRanges);
        
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Day is required")));
    }

    @Test
    void emptyDay_ShouldFail() {
        List<TimeRangeDTO> timeRanges = List.of(new TimeRangeDTO("09:00", "17:00"));
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("", true, timeRanges);
        
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Day is required")));
    }

    @Test
    void blankDay_ShouldFail() {
        List<TimeRangeDTO> timeRanges = List.of(new TimeRangeDTO("09:00", "17:00"));
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("   ", true, timeRanges);
        
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Day is required")));
    }

    @Test
    void nullEnabled_ShouldFail() {
        List<TimeRangeDTO> timeRanges = List.of(new TimeRangeDTO("09:00", "17:00"));
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("MONDAY", null, timeRanges);
        
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Enabled status is required")));
    }

    @Test
    void invalidTimeRangeInList_ShouldFail() {
        List<TimeRangeDTO> timeRanges = List.of(
            new TimeRangeDTO("09:00", "12:00"),
            new TimeRangeDTO("25:00", "17:00")
        );
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("MONDAY", true, timeRanges);
        
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time must be in HH:MM format")));
    }

    @Test
    void multipleInvalidTimeRanges_ShouldFailWithMultipleViolations() {
        List<TimeRangeDTO> timeRanges = List.of(
            new TimeRangeDTO("", "12:00"),
            new TimeRangeDTO("14:00", "25:00"),
            new TimeRangeDTO(null, "18:00")
        );
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("TUESDAY", true, timeRanges);
        
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 3);
    }

    @Test
    void validDayNames_ShouldPass() {
        String[] validDays = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        
        for (String day : validDays) {
            DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO(day, true, Collections.emptyList());
            Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);
            assertTrue(violations.isEmpty(), "Day " + day + " should be valid");
        }
    }

    @Test
    void validCaseInsensitiveDays_ShouldPass() {
        String[] dayVariations = {"monday", "Monday", "MONDAY", "TuEsDay"};
        
        for (String day : dayVariations) {
            DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO(day, true, Collections.emptyList());
            Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

            assertTrue(violations.isEmpty(), "Day variation " + day + " should pass @NotBlank validation");
        }
    }
}