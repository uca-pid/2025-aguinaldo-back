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
        // Arrange
        String validEmail = "test@example.com";

        // Act
        boolean result = emailValidationService.isValidFormat(validEmail);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidFormat_ValidEmailWithSubdomain_ReturnsTrue() {
        // Arrange
        String validEmail = "user@subdomain.example.com";

        // Act
        boolean result = emailValidationService.isValidFormat(validEmail);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidFormat_ValidEmailWithNumbers_ReturnsTrue() {
        // Arrange
        String validEmail = "user123@example123.com";

        // Act
        boolean result = emailValidationService.isValidFormat(validEmail);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidFormat_ValidEmailWithDots_ReturnsTrue() {
        // Arrange
        String validEmail = "user.name@example.com";

        // Act
        boolean result = emailValidationService.isValidFormat(validEmail);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidFormat_ValidEmailWithPlus_ReturnsTrue() {
        // Arrange
        String validEmail = "user+tag@example.com";

        // Act
        boolean result = emailValidationService.isValidFormat(validEmail);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidFormat_EmailWithWhitespace_ReturnsTrue() {
        // Arrange
        String emailWithSpaces = "  test@example.com  ";

        // Act
        boolean result = emailValidationService.isValidFormat(emailWithSpaces);

        // Assert
        assertTrue(result, "Should handle whitespace by trimming");
    }

    @Test
    void isValidFormat_NullEmail_ReturnsFalse() {
        // Arrange
        String nullEmail = null;

        // Act
        boolean result = emailValidationService.isValidFormat(nullEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidFormat_EmptyEmail_ReturnsFalse() {
        // Arrange
        String emptyEmail = "";

        // Act
        boolean result = emailValidationService.isValidFormat(emptyEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidFormat_WhitespaceOnlyEmail_ReturnsFalse() {
        // Arrange
        String whitespaceEmail = "   ";

        // Act
        boolean result = emailValidationService.isValidFormat(whitespaceEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithoutAtSymbol_ReturnsFalse() {
        // Arrange
        String invalidEmail = "testexample.com";

        // Act
        boolean result = emailValidationService.isValidFormat(invalidEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithoutDomain_ReturnsFalse() {
        // Arrange
        String invalidEmail = "test@";

        // Act
        boolean result = emailValidationService.isValidFormat(invalidEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithoutLocalPart_ReturnsFalse() {
        // Arrange
        String invalidEmail = "@example.com";

        // Act
        boolean result = emailValidationService.isValidFormat(invalidEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithMultipleAtSymbols_ReturnsFalse() {
        // Arrange
        String invalidEmail = "test@@example.com";

        // Act
        boolean result = emailValidationService.isValidFormat(invalidEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithInvalidDomain_ReturnsFalse() {
        // Arrange
        String invalidEmail = "test@";

        // Act
        boolean result = emailValidationService.isValidFormat(invalidEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidFormat_EmailWithInvalidSpecialCharacters_ReturnsFalse() {
        // Arrange - using characters that are actually invalid in email addresses
        String invalidEmail = "test@exam<ple.com";

        // Act
        boolean result = emailValidationService.isValidFormat(invalidEmail);

        // Assert
        assertFalse(result);
    }
}