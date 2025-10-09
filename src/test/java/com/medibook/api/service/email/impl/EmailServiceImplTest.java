package com.medibook.api.service.email.impl;

import com.medibook.api.dto.Email.EmailRequestDto;
import com.medibook.api.dto.Email.EmailResponseDto;
import com.medibook.api.service.EmailService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceImplTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testSendWelcomeEmailToPatient() {
        EmailResponseDto response = emailService.sendWelcomeEmailToPatient("patient@example.com", "Juan Pérez");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendApprovalEmailToDoctor() {
        EmailResponseDto response = emailService.sendApprovalEmailToDoctor("doctor@example.com", "Dr. García");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendRejectionEmailToDoctor() {
        EmailResponseDto response = emailService.sendRejectionEmailToDoctor("doctor@example.com", "Dr. García", "Documentación incompleta");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentConfirmationToPatient() {
        EmailResponseDto response = emailService.sendAppointmentConfirmationToPatient(
                "patient@example.com", "Juan Pérez", "Dr. García", "2024-01-15", "10:00");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentConfirmationToDoctor() {
        EmailResponseDto response = emailService.sendAppointmentConfirmationToDoctor(
                "doctor@example.com", "Dr. García", "Juan Pérez", "2024-01-15", "10:00");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentCancellationToPatient() {
        EmailResponseDto response = emailService.sendAppointmentCancellationToPatient(
                "patient@example.com", "Juan Pérez", "Dr. García", "2024-01-15", "10:00");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentCancellationToDoctor() {
        EmailResponseDto response = emailService.sendAppointmentCancellationToDoctor(
                "doctor@example.com", "Dr. García", "Juan Pérez", "2024-01-15", "10:00");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentModificationApprovedToPatient() {
        EmailResponseDto response = emailService.sendAppointmentModificationApprovedToPatient(
                "patient@example.com", "Juan Pérez", "Dr. García",
                "2024-01-15", "10:00", "2024-01-16", "11:00");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentModificationApprovedToDoctor() {
        EmailResponseDto response = emailService.sendAppointmentModificationApprovedToDoctor(
                "doctor@example.com", "Dr. García", "Juan Pérez",
                "2024-01-15", "10:00", "2024-01-16", "11:00");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendEmail() {
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject("Test Subject")
                .htmlContent("<p>Test HTML content</p>")
                .textContent("Test text content")
                .build();

        EmailResponseDto response = emailService.sendEmail(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }
}