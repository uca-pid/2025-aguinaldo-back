package com.medibook.api.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

/**
 * Servicio para validación básica de emails
 * - Validación de formato
 */
@Service
@Slf4j
public class EmailValidationService {

    private final EmailValidator emailValidator = EmailValidator.getInstance();

    /**
     * Valida el formato básico del email
     */
    public boolean isValidFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return emailValidator.isValid(email.trim());
    }
}