package com.medibook.api.service;

import com.medibook.api.dto.email.EmailRequestDto;
import com.medibook.api.dto.email.EmailResponseDto;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    
    CompletableFuture<EmailResponseDto> sendEmailAsync(EmailRequestDto emailRequest);
    
    CompletableFuture<EmailResponseDto> sendWelcomeEmailToPatientAsync(String patientEmail, String patientName);
    
    CompletableFuture<EmailResponseDto> sendApprovalEmailToDoctorAsync(String doctorEmail, String doctorName);
    
    CompletableFuture<EmailResponseDto> sendRejectionEmailToDoctorAsync(String doctorEmail, String doctorName, String reason);
    
    CompletableFuture<EmailResponseDto> sendAppointmentConfirmationToPatientAsync(String patientEmail, String patientName, 
                                                                                String doctorName, String appointmentDate, String appointmentTime,
                                                                                String turnId);
    
    CompletableFuture<EmailResponseDto> sendAppointmentConfirmationToDoctorAsync(String doctorEmail, String doctorName, 
                                                                               String patientName, String appointmentDate, String appointmentTime,
                                                                               String turnId);
    
    CompletableFuture<EmailResponseDto> sendAppointmentCancellationToPatientAsync(String patientEmail, String patientName, 
                                                                                String doctorName, String appointmentDate, String appointmentTime);
    
    CompletableFuture<EmailResponseDto> sendAppointmentCancellationToDoctorAsync(String doctorEmail, String doctorName, 
                                                                               String patientName, String appointmentDate, String appointmentTime);
    
    CompletableFuture<EmailResponseDto> sendAppointmentModificationApprovedToPatientAsync(String patientEmail, String patientName, 
                                                                                        String doctorName, String oldDate, String oldTime,
                                                                                        String newDate, String newTime);
    
    CompletableFuture<EmailResponseDto> sendAppointmentModificationApprovedToDoctorAsync(String doctorEmail, String doctorName, 
                                                                                       String patientName, String oldDate, String oldTime,
                                                                                       String newDate, String newTime);
}