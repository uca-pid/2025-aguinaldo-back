package com.medibook.api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestDTOTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllRequiredFieldsPresent_thenNoViolations() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "test@example.com",
            "password123",
            "John",
            "Doe",
            "+1234567890",
            LocalDate.of(1990, 1, 1),
            "MALE",
            null,
            null,
            null
        );

        var violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenEmailIsInvalid_thenViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "invalid-email",
            "password123",
            "John",
            "Doe",
            "+1234567890",
            LocalDate.of(1990, 1, 1),
            "MALE",
            null,
            null,
            null
        );

        var violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Invalid email format", violations.iterator().next().getMessage());
    }

    @Test
    void whenPasswordTooShort_thenViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "test@example.com",
            "short",
            "John",
            "Doe",
            "+1234567890",
            LocalDate.of(1990, 1, 1),
            "MALE",
            null,
            null,
            null
        );

        var violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Password must be at least 8 characters long", violations.iterator().next().getMessage());
    }

    @Test 
    void whenOptionalFieldsMissing_thenNoViolations() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "test@example.com",
            "password123",
            "John",
            "Doe",
            null,
            null,
            null,
            null,
            null,
            null
        );

        var violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenSlotDurationIsInvalid_thenViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "test@example.com",
            "password123",
            "John",
            "Doe",
            null,
            null,
            null,
            "ML123",
            "Cardiology",
            -1
        );

        var violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Slot duration must be positive", violations.iterator().next().getMessage());
    }
}