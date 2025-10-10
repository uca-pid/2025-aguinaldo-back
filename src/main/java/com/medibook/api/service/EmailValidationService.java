package com.medibook.api.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * Servicio para validación avanzada de emails
 * - Validación de formato
 * - Verificación de dominio MX (opcional)
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

    /**
     * Valida si el dominio del email tiene registros MX
     * (indica que puede recibir emails)
     */
    public boolean hasValidMXRecord(String email) {
        if (!isValidFormat(email)) {
            return false;
        }

        try {
            String domain = email.substring(email.indexOf("@") + 1);
            return checkMXRecord(domain);
        } catch (Exception e) {
            log.warn("No se pudo verificar MX record para {}: {}", email, e.getMessage());
            return true;
        }
    }

    private boolean checkMXRecord(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            
            DirContext dirContext = new InitialDirContext(env);
            Attributes attributes = dirContext.getAttributes(domain, new String[]{"MX"});
            Attribute mxAttribute = attributes.get("MX");
            
            boolean hasMX = mxAttribute != null && mxAttribute.size() > 0;
            
            if (hasMX) {
                log.debug("Dominio {} tiene registros MX válidos", domain);
            } else {
                log.debug("Dominio {} no tiene registros MX", domain);
            }
            
            dirContext.close();
            return hasMX;
            
        } catch (NamingException e) {
            log.debug("Error verificando MX para {}: {}", domain, e.getMessage());
            return true;
        }
    }

    /**
     * Validación completa: formato + MX record
     */
    public boolean isValidEmail(String email) {
        return isValidFormat(email) && hasValidMXRecord(email);
    }

    /**
     * Validación rápida solo de formato (para usar en producción)
     */
    public boolean isValidEmailQuick(String email) {
        return isValidFormat(email);
    }
}