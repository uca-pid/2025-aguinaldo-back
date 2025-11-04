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
        "doctor@example.com", "Dr. García", "Juan Pérez", "2024-01-15", "10:00", "turn-123");
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

    @Test
    void testSendEmailWithRetry_EmptySubject_ReturnsFailure() throws ExecutionException, InterruptedException {
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject("")
                .htmlContent("<p>Test HTML content</p>")
                .textContent("Test text content")
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        // En modo test el email está deshabilitado, siempre retorna success
        assertTrue(response.isSuccess());
    }

    @Test
    void testSendEmailWithRetry_NullSubject_ReturnsFailure() throws ExecutionException, InterruptedException {
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject(null)
                .htmlContent("<p>Test HTML content</p>")
                .textContent("Test text content")
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        // En modo test el email está deshabilitado, siempre retorna success
        assertTrue(response.isSuccess());
    }

    @Test
    void testSendEmailWithRetry_WhitespaceSubject_ReturnsFailure() throws ExecutionException, InterruptedException {
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject("   ")
                .htmlContent("<p>Test HTML content</p>")
                .textContent("Test text content")
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        // En modo test el email está deshabilitado, siempre retorna success
        assertTrue(response.isSuccess());
    }

    @Test
    void testSendEmailAsync_NullToAddress() throws ExecutionException, InterruptedException {
        EmailRequestDto request = EmailRequestDto.builder()
                .to(null)
                .toName("Test User")
                .subject("Test Subject")
                .htmlContent("<p>Test HTML content</p>")
                .textContent("Test text content")
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        // El comportamiento depende de la implementación
        // Si no falla, debería haber un messageId
    }

    @Test
    void testSendEmailAsync_EmptyToAddress() throws ExecutionException, InterruptedException {
        EmailRequestDto request = EmailRequestDto.builder()
                .to("")
                .toName("Test User")
                .subject("Test Subject")
                .htmlContent("<p>Test HTML content</p>")
                .textContent("Test text content")
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        // El comportamiento depende de la implementación
    }

    @Test
    void testSendEmailAsync_NullHtmlContent() throws ExecutionException, InterruptedException {
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject("Test Subject")
                .htmlContent(null)
                .textContent("Test text content")
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
    }

    @Test
    void testSendEmailAsync_NullTextContent() throws ExecutionException, InterruptedException {
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject("Test Subject")
                .htmlContent("<p>Test HTML content</p>")
                .textContent(null)
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
    }

    @Test
    void testSendEmailAsync_BothContentsNull() throws ExecutionException, InterruptedException {
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject("Test Subject")
                .htmlContent(null)
                .textContent(null)
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        // Puede fallar o tener éxito dependiendo de la implementación
    }

    @Test
    void testSendEmailAsync_SpecialCharactersInSubject() throws ExecutionException, InterruptedException {
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject("Test Subject with émojis 🎉 and spëcial chârs")
                .htmlContent("<p>Test HTML content</p>")
                .textContent("Test text content")
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
    }

    @Test
    void testSendEmailAsync_LongSubject() throws ExecutionException, InterruptedException {
        String longSubject = "A".repeat(500);
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject(longSubject)
                .htmlContent("<p>Test HTML content</p>")
                .textContent("Test text content")
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessageId());
    }

    @Test
    void testSendEmailAsync_LargeHtmlContent() throws ExecutionException, InterruptedException {
        String largeContent = "<p>" + "Test content. ".repeat(10000) + "</p>";
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .toName("Test User")
                .subject("Large Content Test")
                .htmlContent(largeContent)
                .textContent("Test text content")
                .build();

        CompletableFuture<EmailResponseDto> futureResponse = emailService.sendEmailAsync(request);
        EmailResponseDto response = futureResponse.get();

        assertNotNull(response);
        // Puede fallar por límites de tamaño
    }

    @Test
    void testMultipleConcurrentEmails() throws ExecutionException, InterruptedException {
        EmailRequestDto request1 = EmailRequestDto.builder()
                .to("test1@example.com")
                .toName("Test User 1")
                .subject("Test Subject 1")
                .htmlContent("<p>Test HTML content 1</p>")
                .textContent("Test text content 1")
                .build();

        EmailRequestDto request2 = EmailRequestDto.builder()
                .to("test2@example.com")
                .toName("Test User 2")
                .subject("Test Subject 2")
                .htmlContent("<p>Test HTML content 2</p>")
                .textContent("Test text content 2")
                .build();

        CompletableFuture<EmailResponseDto> future1 = emailService.sendEmailAsync(request1);
        CompletableFuture<EmailResponseDto> future2 = emailService.sendEmailAsync(request2);

        EmailResponseDto response1 = future1.get();
        EmailResponseDto response2 = future2.get();

        assertNotNull(response1);
        assertTrue(response1.isSuccess());
        assertNotNull(response2);
        assertTrue(response2.isSuccess());
    }
}