package com.medibook.api.dto.validation;

import com.medibook.api.dto.Availability.TimeRangeDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TimeRangeDTOValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validTimeRange_ShouldPass() {
        TimeRangeDTO timeRange = new TimeRangeDTO("09:00", "17:00");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void validTimeRange_MidnightTimes_ShouldPass() {
        TimeRangeDTO timeRange = new TimeRangeDTO("00:00", "23:59");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void validTimeRange_SingleDigitHour_ShouldPass() {
        TimeRangeDTO timeRange = new TimeRangeDTO("9:30", "5:45");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullStartTime_ShouldFail() {
        TimeRangeDTO timeRange = new TimeRangeDTO(null, "17:00");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time is required")));
    }

    @Test
    void emptyStartTime_ShouldFail() {
        TimeRangeDTO timeRange = new TimeRangeDTO("", "17:00");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time is required")));
    }

    @Test
    void blankStartTime_ShouldFail() {
        TimeRangeDTO timeRange = new TimeRangeDTO("   ", "17:00");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time is required")));
    }

    @Test
    void nullEndTime_ShouldFail() {
        TimeRangeDTO timeRange = new TimeRangeDTO("09:00", null);
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("End time is required")));
    }

    @Test
    void emptyEndTime_ShouldFail() {
        TimeRangeDTO timeRange = new TimeRangeDTO("09:00", "");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("End time is required")));
    }

    @Test
    void invalidStartTimeFormat_ShouldFail() {
        TimeRangeDTO timeRange = new TimeRangeDTO("25:00", "17:00");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time must be in HH:MM format")));
    }

    @Test
    void invalidEndTimeFormat_ShouldFail() {
        TimeRangeDTO timeRange = new TimeRangeDTO("09:00", "17:60");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("End time must be in HH:MM format")));
    }

    @Test
    void invalidTimeFormat_NoColon_ShouldFail() {
        TimeRangeDTO timeRange = new TimeRangeDTO("0900", "1700");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time must be in HH:MM format")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("End time must be in HH:MM format")));
    }

    @Test
    void invalidTimeFormat_WrongPattern_ShouldFail() {
        TimeRangeDTO timeRange = new TimeRangeDTO("9:5", "17:5");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time must be in HH:MM format")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("End time must be in HH:MM format")));
    }

    @Test
    void invalidTimeFormat_Letters_ShouldFail() {
        TimeRangeDTO timeRange = new TimeRangeDTO("09:AA", "BB:00");
        
        Set<ConstraintViolation<TimeRangeDTO>> violations = validator.validate(timeRange);
        
        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Start time must be in HH:MM format")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("End time must be in HH:MM format")));
    }
}