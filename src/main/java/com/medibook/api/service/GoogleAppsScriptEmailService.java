package com.medibook.api.service;

import com.medibook.api.dto.email.EmailRequestDto;
import com.medibook.api.dto.email.EmailResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para enviar emails usando Google Apps Script
 * Reemplaza el envío directo por SMTP por un endpoint de Google Apps Script
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAppsScriptEmailService {

    private final RestTemplate restTemplate;
    
    @Value("${google.apps.script.url}")
    private String googleAppsScriptUrl;
    
    @Value("${google.apps.script.token}")
    private String secretToken;

    /**
     * Envía un email usando Google Apps Script
     * @param emailRequest Los datos del email a enviar
     * @return EmailResponseDto con el resultado del envío
     */
    public EmailResponseDto sendEmail(EmailRequestDto emailRequest) {
        try {
            log.debug("Sending email via Google Apps Script to: {}", emailRequest.getTo());
            
            // Preparar los datos para enviar al script
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("token", secretToken);
            requestData.put("to", emailRequest.getTo());
            requestData.put("subject", emailRequest.getSubject());
            requestData.put("body", emailRequest.getTextContent() != null ? emailRequest.getTextContent() : "");
            
            // Si hay contenido HTML, lo incluimos
            if (emailRequest.getHtmlContent() != null && !emailRequest.getHtmlContent().trim().isEmpty()) {
                requestData.put("htmlBody", emailRequest.getHtmlContent());
            }

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

            long startTime = System.currentTimeMillis();
            
            // Realizar la petición POST al Google Apps Script
            ResponseEntity<String> response = restTemplate.postForEntity(
                googleAppsScriptUrl, 
                entity, 
                String.class
            );

            long duration = System.currentTimeMillis() - startTime;

            // Verificar la respuesta
            if (response.getStatusCode() == HttpStatus.OK && "OK".equals(response.getBody())) {
                log.info("Email sent successfully via Google Apps Script to: {} | Subject: {} | Time: {}ms", 
                        emailRequest.getTo(), emailRequest.getSubject(), duration);
                        
                return EmailResponseDto.builder()
                        .success(true)
                        .messageId("google-script-" + System.currentTimeMillis())
                        .message("Email sent successfully via Google Apps Script")
                        .build();
            } else if ("Unauthorized".equals(response.getBody())) {
                log.error("Unauthorized access to Google Apps Script - invalid token");
                return EmailResponseDto.builder()
                        .success(false)
                        .message("Unauthorized access to email service")
                        .errorDetails("Invalid authentication token")
                        .build();
            } else {
                log.error("Unexpected response from Google Apps Script: {}", response.getBody());
                return EmailResponseDto.builder()
                        .success(false)
                        .message("Unexpected response from email service")
                        .errorDetails("Response: " + response.getBody())
                        .build();
            }

        } catch (RestClientException e) {
            log.error("Error calling Google Apps Script for email to {}: {}", 
                    emailRequest.getTo(), e.getMessage());
            return EmailResponseDto.builder()
                    .success(false)
                    .message("Error communicating with email service")
                    .errorDetails("RestClient error: " + e.getMessage())
                    .build();
                    
        } catch (Exception e) {
            log.error("General error sending email via Google Apps Script to {}: {}", 
                    emailRequest.getTo(), e.getMessage());
            return EmailResponseDto.builder()
                    .success(false)
                    .message("General error sending email")
                    .errorDetails("Error: " + e.getMessage())
                    .build();
        }
    }
}