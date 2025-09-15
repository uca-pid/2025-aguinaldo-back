package com.medibook.api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.medibook.api.dto.Auth.RegisterRequestDTO;

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
            12345678L,
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
            12345678L,
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
            12345678L,
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
            12345678L,
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
            12345678L,
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

    @Test
    void whenDniIsNull_thenViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "test@example.com",
            null,
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
        assertEquals(1, violations.size());
        assertEquals("DNI is required", violations.iterator().next().getMessage());
    }

    @Test
    void whenDniTooShort_thenViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "test@example.com",
            123456L, // 6 digits, too short
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
        assertEquals(1, violations.size());
        assertEquals("DNI must be at least 7 digits", violations.iterator().next().getMessage());
    }

    @Test
    void whenDniTooLong_thenViolation() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
            "test@example.com",
            1234567890L, // 10 digits, too long
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
        assertEquals(1, violations.size());
        assertEquals("DNI must be at most 9 digits", violations.iterator().next().getMessage());
    }
}