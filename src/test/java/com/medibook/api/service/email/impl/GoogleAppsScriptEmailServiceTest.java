package com.medibook.api.service.email.impl;

import com.medibook.api.dto.email.EmailRequestDto;
import com.medibook.api.dto.email.EmailResponseDto;
import com.medibook.api.service.GoogleAppsScriptEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GoogleAppsScriptEmailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private GoogleAppsScriptEmailService googleAppsScriptEmailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        googleAppsScriptEmailService = new GoogleAppsScriptEmailService(restTemplate);
        
        // Configurar propiedades usando reflection
        ReflectionTestUtils.setField(googleAppsScriptEmailService, "googleAppsScriptUrl", 
                "https://script.google.com/macros/s/test/exec");
        ReflectionTestUtils.setField(googleAppsScriptEmailService, "secretToken", "testToken");
    }

    @Test
    void testSendEmailSuccess() {
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .textContent("Test content")
                .htmlContent("<h1>Test HTML</h1>")
                .build();

        ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenReturn(mockResponse);

        EmailResponseDto result = googleAppsScriptEmailService.sendEmail(emailRequest);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Email sent successfully via Google Apps Script", result.getMessage());
        assertNotNull(result.getMessageId());
        assertTrue(result.getMessageId().startsWith("google-script-"));
    }

    @Test
    void testSendEmailUnauthorized() {
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .textContent("Test content")
                .build();

        ResponseEntity<String> mockResponse = new ResponseEntity<>("Unauthorized", HttpStatus.OK);
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenReturn(mockResponse);

        EmailResponseDto result = googleAppsScriptEmailService.sendEmail(emailRequest);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Unauthorized access to email service", result.getMessage());
        assertEquals("Invalid authentication token", result.getErrorDetails());
    }

    @Test
    void testSendEmailRestClientException() {
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .textContent("Test content")
                .build();

        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenThrow(new RestClientException("Connection failed"));

        EmailResponseDto result = googleAppsScriptEmailService.sendEmail(emailRequest);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Error communicating with email service", result.getMessage());
        assertTrue(result.getErrorDetails().contains("RestClient error"));
    }

    @Test
    void testSendEmailOnlyTextContent() {
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .textContent("Test content only")
                .build();

        ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenReturn(mockResponse);

        EmailResponseDto result = googleAppsScriptEmailService.sendEmail(emailRequest);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }
}