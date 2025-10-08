package com.medibook.api.service.email.impl;

import com.medibook.api.dto.email.EmailRequestDto;
import com.medibook.api.dto.email.EmailResponseDto;
import com.medibook.api.service.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceImplTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Este test requiere configuración de MailerSend
        // Se puede usar un mock para testing unitario
    }

    @Test
    void testEmailRequestDtoValidation() {
        // Test para validar la estructura del DTO
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject("Test Subject")
                .htmlContent("<p>Test HTML content</p>")
                .textContent("Test text content")
                .build();

        assertNotNull(request.getTo());
        assertNotNull(request.getToName());
        assertNotNull(request.getSubject());
        assertNotNull(request.getHtmlContent());
        assertNotNull(request.getTextContent());
    }

    @Test
    void testEmailResponseDtoCreation() {
        // Test para validar la estructura del DTO de respuesta
        EmailResponseDto response = EmailResponseDto.builder()
                .success(true)
                .messageId("test-message-id")
                .message("Email sent successfully")
                .build();

        assertTrue(response.isSuccess());
        assertEquals("test-message-id", response.getMessageId());
        assertEquals("Email sent successfully", response.getMessage());
    }

    // Nota: Para testing real con MailerSend, necesitarás:
    // 1. Un token de API válido de MailerSend
    // 2. Un dominio verificado
    // 3. Variables de entorno configuradas correctamente
}