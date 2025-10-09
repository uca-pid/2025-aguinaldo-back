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
    void validDoctorAvailabilityRequest_ShouldPass() {
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", true, List.of(
                new TimeRangeDTO("09:00", "12:00"),
                new TimeRangeDTO("14:00", "18:00")
            )),
            new DayAvailabilityDTO("TUESDAY", true, List.of(
                new TimeRangeDTO("10:00", "15:00")
            )),
            new DayAvailabilityDTO("FRIDAY", true, List.of(
                new TimeRangeDTO("09:00", "17:00")
            )),
            new DayAvailabilityDTO("SATURDAY", false, Collections.emptyList()),
            new DayAvailabilityDTO("SUNDAY", false, Collections.emptyList())
        );
        
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(weeklyAvailability);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullWeeklyAvailability_ShouldFail() {
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(null);
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Weekly availability is required", violations.iterator().next().getMessage());
    }

    @Test
    void emptyWeeklyAvailability_ShouldPass() {
        DoctorAvailabilityRequestDTO request = new DoctorAvailabilityRequestDTO(Collections.emptyList());
        
        Set<ConstraintViolation<DoctorAvailabilityRequestDTO>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty());
    }
}