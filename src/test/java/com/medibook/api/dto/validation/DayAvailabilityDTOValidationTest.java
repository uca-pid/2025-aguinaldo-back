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
        // Arrange
        List<TimeRangeDTO> timeRanges = List.of(
            new TimeRangeDTO("09:00", "12:00"),
            new TimeRangeDTO("14:00", "18:00")
        );
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("MONDAY", true, timeRanges);

        // Act
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validDayAvailability_EmptyRanges_ShouldPass() {
        // Arrange
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("SUNDAY", false, Collections.emptyList());

        // Act
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullDay_ShouldFail() {
        // Arrange
        List<TimeRangeDTO> timeRanges = List.of(new TimeRangeDTO("09:00", "17:00"));
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO(null, true, timeRanges);

        // Act
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Day is required")));
    }

    @Test
    void emptyDay_ShouldFail() {
        // Arrange
        List<TimeRangeDTO> timeRanges = List.of(new TimeRangeDTO("09:00", "17:00"));
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("", true, timeRanges);

        // Act
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Day is required")));
    }

    @Test
    void blankDay_ShouldFail() {
        // Arrange
        List<TimeRangeDTO> timeRanges = List.of(new TimeRangeDTO("09:00", "17:00"));
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("   ", true, timeRanges);

        // Act
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Day is required")));
    }

    @Test
    void nullEnabled_ShouldFail() {
        // Arrange
        List<TimeRangeDTO> timeRanges = List.of(new TimeRangeDTO("09:00", "17:00"));
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("MONDAY", null, timeRanges);

        // Act
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Enabled status is required")));
    }

    @Test
    void invalidTimeRangeInList_ShouldFail() {
        // Arrange - One valid time range and one invalid
        List<TimeRangeDTO> timeRanges = List.of(
            new TimeRangeDTO("09:00", "12:00"),  // Valid
            new TimeRangeDTO("25:00", "17:00")   // Invalid start time
        );
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("MONDAY", true, timeRanges);

        // Act
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time must be in HH:MM format")));
    }

    @Test
    void multipleInvalidTimeRanges_ShouldFailWithMultipleViolations() {
        // Arrange - Multiple invalid time ranges
        List<TimeRangeDTO> timeRanges = List.of(
            new TimeRangeDTO("", "12:00"),       // Empty start time
            new TimeRangeDTO("14:00", "25:00"),  // Invalid end time
            new TimeRangeDTO(null, "18:00")      // Null start time
        );
        DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO("TUESDAY", true, timeRanges);

        // Act
        Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 3); // At least one violation per invalid time range
    }

    @Test
    void validDayNames_ShouldPass() {
        // Test all valid day names
        String[] validDays = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        
        for (String day : validDays) {
            // Arrange
            DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO(day, true, Collections.emptyList());

            // Act
            Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

            // Assert
            assertTrue(violations.isEmpty(), "Day " + day + " should be valid");
        }
    }

    @Test
    void validCaseInsensitiveDays_ShouldPass() {
        // Arrange - Different cases should be valid (validation doesn't enforce specific case)
        String[] dayVariations = {"monday", "Monday", "MONDAY", "TuEsDay"};
        
        for (String day : dayVariations) {
            DayAvailabilityDTO dayAvailability = new DayAvailabilityDTO(day, true, Collections.emptyList());

            // Act
            Set<ConstraintViolation<DayAvailabilityDTO>> violations = validator.validate(dayAvailability);

            // Assert - Only @NotBlank validation applies, not specific day name validation
            assertTrue(violations.isEmpty(), "Day variation " + day + " should pass @NotBlank validation");
        }
    }
}