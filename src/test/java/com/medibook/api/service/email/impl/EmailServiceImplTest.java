package com.medibook.api.service.email.impl;

import com.medibook.api.dto.email.EmailRequestDto;
import com.medibook.api.dto.email.EmailResponseDto;
import com.medibook.api.service.EmailService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceImplTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testSendWelcomeEmailToPatientAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendWelcomeEmailToPatientAsync("patient@example.com", "Juan Pérez");
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendApprovalEmailToDoctorAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendApprovalEmailToDoctorAsync("doctor@example.com", "Dr. García");
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendRejectionEmailToDoctorAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendRejectionEmailToDoctorAsync("doctor@example.com", "Dr. García", "Documentación incompleta");
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentConfirmationToPatientAsync() throws ExecutionException, InterruptedException {
    CompletableFuture<EmailResponseDto> futureResponse = emailService.sendAppointmentConfirmationToPatientAsync(
        "patient@example.com", "Juan Pérez", "Dr. García", "2024-01-15", "10:00", "turn-123");
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentConfirmationToDoctorAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendAppointmentConfirmationToDoctorAsync(
                "doctor@example.com", "Dr. García", "Juan Pérez", "2024-01-15", "10:00");
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentCancellationToPatientAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendAppointmentCancellationToPatientAsync(
                "patient@example.com", "Juan Pérez", "Dr. García", "2024-01-15", "10:00");
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentCancellationToDoctorAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendAppointmentCancellationToDoctorAsync(
                "doctor@example.com", "Dr. García", "Juan Pérez", "2024-01-15", "10:00");
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentModificationApprovedToPatientAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendAppointmentModificationApprovedToPatientAsync(
                "patient@example.com", "Juan Pérez", "Dr. García",
                "2024-01-15", "10:00", "2024-01-16", "11:00");
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendAppointmentModificationApprovedToDoctorAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendAppointmentModificationApprovedToDoctorAsync(
                "doctor@example.com", "Dr. García", "Juan Pérez",
                "2024-01-15", "10:00", "2024-01-16", "11:00");
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }

    @Test
    void testSendEmailAsync() throws ExecutionException, InterruptedException {
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject("Test Subject")
                .htmlContent("<p>Test HTML content</p>")
                .textContent("Test text content")
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
        assertNotNull(response.getMessage());
    }
}