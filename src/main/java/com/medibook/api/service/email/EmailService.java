package com.medibook.api.service.email;

import com.medibook.api.dto.email.EmailRequestDto;
import com.medibook.api.dto.email.EmailResponseDto;

public interface EmailService {
    
    EmailResponseDto sendEmail(EmailRequestDto emailRequest);
    
    EmailResponseDto sendWelcomeEmailToPatient(String patientEmail, String patientName);
    
    EmailResponseDto sendApprovalEmailToDoctor(String doctorEmail, String doctorName);
    
    EmailResponseDto sendRejectionEmailToDoctor(String doctorEmail, String doctorName, String reason);
    
    EmailResponseDto sendAppointmentConfirmationToPatient(String patientEmail, String patientName, 
                                                        String doctorName, String appointmentDate, String appointmentTime);
    
    EmailResponseDto sendAppointmentConfirmationToDoctor(String doctorEmail, String doctorName, 
                                                       String patientName, String appointmentDate, String appointmentTime);
    
    EmailResponseDto sendAppointmentCancellationToPatient(String patientEmail, String patientName, 
                                                        String doctorName, String appointmentDate, String appointmentTime);
    
    EmailResponseDto sendAppointmentCancellationToDoctor(String doctorEmail, String doctorName, 
                                                       String patientName, String appointmentDate, String appointmentTime);
    
    EmailResponseDto sendAppointmentModificationApprovedToPatient(String patientEmail, String patientName, 
                                                                String doctorName, String oldDate, String oldTime,
                                                                String newDate, String newTime);
    
    EmailResponseDto sendAppointmentModificationApprovedToDoctor(String doctorEmail, String doctorName, 
                                                               String patientName, String oldDate, String oldTime,
                                                               String newDate, String newTime);
}