package com.medibook.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailValidationServiceTest {

    private EmailValidationService emailValidationService;

    @BeforeEach
    void setUp() {
        emailValidationService = new EmailValidationService();
    }

    @Test
    void isValidFormat_ValidEmail_ReturnsTrue() {
        String validEmail = "test@example.com";

        boolean result = emailValidationService.isValidFormat(validEmail);

        assertTrue(result);
    }

    @Test
    void isValidFormat_ValidEmailWithSubdomain_ReturnsTrue() {
        String validEmail = "user@subdomain.example.com";

        boolean result = emailValidationService.isValidFormat(validEmail);

        assertTrue(result);
    }

    @Test
    void isValidFormat_ValidEmailWithNumbers_ReturnsTrue() {
        String validEmail = "user123@example123.com";

        boolean result = emailValidationService.isValidFormat(validEmail);

        assertTrue(result);
    }

    @Test
    void isValidFormat_ValidEmailWithDots_ReturnsTrue() {
        String validEmail = "user.name@example.com";

        boolean result = emailValidationService.isValidFormat(validEmail);

        assertTrue(result);
    }

    @Test
    void isValidFormat_ValidEmailWithPlus_ReturnsTrue() {
        String validEmail = "user+tag@example.com";

        boolean result = emailValidationService.isValidFormat(validEmail);

        assertTrue(result);
    }

    @Test
    void isValidFormat_EmailWithWhitespace_ReturnsTrue() {
        String emailWithSpaces = "  test@example.com  ";

        boolean result = emailValidationService.isValidFormat(emailWithSpaces);

        assertTrue(result, "Should handle whitespace by trimming");
    }

    @Test
    void isValidFormat_NullEmail_ReturnsFalse() {
        String nullEmail = null;

        boolean result = emailValidationService.isValidFormat(nullEmail);

        assertFalse(result);
    }

    @Test
    void isValidFormat_EmptyEmail_ReturnsFalse() {
        String emptyEmail = "";

        boolean result = emailValidationService.isValidFormat(emptyEmail);

        assertFalse(result);
    }

    @Test
    void isValidFormat_WhitespaceOnlyEmail_ReturnsFalse() {
        String whitespaceEmail = "   ";

        boolean result = emailValidationService.isValidFormat(whitespaceEmail);

        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithoutAtSymbol_ReturnsFalse() {
        String invalidEmail = "testexample.com";

        boolean result = emailValidationService.isValidFormat(invalidEmail);

        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithoutDomain_ReturnsFalse() {
        String invalidEmail = "test@";

        boolean result = emailValidationService.isValidFormat(invalidEmail);

        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithoutLocalPart_ReturnsFalse() {
        String invalidEmail = "@example.com";

        boolean result = emailValidationService.isValidFormat(invalidEmail);

        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithMultipleAtSymbols_ReturnsFalse() {
        String invalidEmail = "test@@example.com";

        boolean result = emailValidationService.isValidFormat(invalidEmail);

        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithInvalidDomain_ReturnsFalse() {
        String invalidEmail = "test@";

        boolean result = emailValidationService.isValidFormat(invalidEmail);

        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithInvalidSpecialCharacters_ReturnsFalse() {
        // Arrange - using characters that are actually invalid in email addresses
        String invalidEmail = "test@exam<ple.com";

        boolean result = emailValidationService.isValidFormat(invalidEmail);

        assertFalse(result);
    }
}