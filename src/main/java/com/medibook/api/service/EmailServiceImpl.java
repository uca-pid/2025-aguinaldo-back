package com.medibook.api.service;

import com.medibook.api.dto.email.EmailRequestDto;
import com.medibook.api.dto.email.EmailResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailValidationService emailValidationService;
    
    @Value("${email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${email.from.email}")
    private String fromEmail;
    
    @Value("${email.from.name}")
    private String fromName;
    
    @Value("${email.validation.mx.enabled:false}")
    private boolean mxValidationEnabled;

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<EmailResponseDto> sendEmailAsync(EmailRequestDto emailRequest) {
        try {
            EmailResponseDto result = sendEmailWithRetry(emailRequest);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            EmailResponseDto failureResult = recoverFromEmailFailure(e, emailRequest);
            return CompletableFuture.completedFuture(failureResult);
        }
    }

    @Retryable(
        value = {MessagingException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    private EmailResponseDto sendEmailWithRetry(EmailRequestDto emailRequest) throws Exception {
        
        if (!emailEnabled) {
            log.info("Email disabled - simulating send to: {} | Subject: {}", 
                    emailRequest.getTo(), emailRequest.getSubject());
            return EmailResponseDto.builder()
                    .success(true)
                    .messageId("dev-mode-" + System.currentTimeMillis())
                    .message("Email simulated in development mode")
                    .build();
        }
        
        if (!emailValidationService.isValidFormat(emailRequest.getTo())) {
            log.error("Invalid email format: {}", emailRequest.getTo());
            return EmailResponseDto.builder()
                    .success(false)
                    .message("Invalid email format")
                    .errorDetails("Email: " + emailRequest.getTo())
                    .build();
        }
        
        if (mxValidationEnabled && !emailValidationService.hasValidMXRecord(emailRequest.getTo())) {
            log.warn("Email without valid MX records: {}", emailRequest.getTo());
            return EmailResponseDto.builder()
                    .success(false)
                    .message("Email domain cannot receive emails")
                    .errorDetails("No MX records for: " + emailRequest.getTo())
                    .build();
        }
        
        if (emailRequest.getSubject() == null || emailRequest.getSubject().trim().isEmpty()) {
            log.error("Empty email subject for: {}", emailRequest.getTo());
            return EmailResponseDto.builder()
                    .success(false)
                    .message("Email subject is required")
                    .build();
        }
        
        try {
            long startTime = System.currentTimeMillis();
            
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            
            if (emailRequest.getHtmlContent() != null && !emailRequest.getHtmlContent().trim().isEmpty()) {
                helper.setText(
                    emailRequest.getTextContent() != null ? emailRequest.getTextContent() : "", 
                    emailRequest.getHtmlContent()
                );
            } else {
                helper.setText(emailRequest.getTextContent() != null ? emailRequest.getTextContent() : "", false);
            }
            
            javaMailSender.send(message);
            
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Email sent successfully to: {} | Subject: {} | Time: {}ms", 
                    emailRequest.getTo(), emailRequest.getSubject(), duration);
            
            return EmailResponseDto.builder()
                    .success(true)
                    .messageId("gmail-" + System.currentTimeMillis())
                    .message("Email sent successfully")
                    .build();
                    
        } catch (MessagingException e) {
            log.error("Messaging error sending email to {}: {}", emailRequest.getTo(), e.getMessage());
            throw new RuntimeException("Messaging error: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("General error sending email to {}: {}", emailRequest.getTo(), e.getMessage());
            throw new RuntimeException("General error: " + e.getMessage(), e);
        }
    }

    @Recover
    public EmailResponseDto recoverFromEmailFailure(Exception ex, EmailRequestDto emailRequest) {
        log.error("Definitive failure sending email to {} after all retries: {}", 
                emailRequest.getTo(), ex.getMessage());
        
        return EmailResponseDto.builder()
                .success(false)
                .message("Definitive error sending email after retries")
                .errorDetails("Error: " + ex.getMessage())
                .build();
    }

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<EmailResponseDto> sendWelcomeEmailToPatientAsync(String patientEmail, String patientName) {
        log.debug("Processing welcome email async to: {}", patientEmail);
        String subject = "Bienvenido a MediBook";
        String htmlContent = buildWelcomePatientHtml(patientName);
        String textContent = buildWelcomePatientText(patientName);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(patientEmail)
                .toName(patientName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmailAsync(emailRequest);
    }

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<EmailResponseDto> sendApprovalEmailToDoctorAsync(String doctorEmail, String doctorName) {
        log.debug("Processing approval email async to doctor: {}", doctorEmail);
        String subject = "Tu registro como doctor ha sido aprobado";
        String htmlContent = buildDoctorApprovalHtml(doctorName);
        String textContent = buildDoctorApprovalText(doctorName);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(doctorEmail)
                .toName(doctorName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmailAsync(emailRequest);
    }

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<EmailResponseDto> sendRejectionEmailToDoctorAsync(String doctorEmail, String doctorName, String reason) {
        log.debug("Processing rejection email async to doctor: {}", doctorEmail);
        String subject = "Actualizacion sobre tu registro como doctor";
        String htmlContent = buildDoctorRejectionHtml(doctorName, reason);
        String textContent = buildDoctorRejectionText(doctorName, reason);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(doctorEmail)
                .toName(doctorName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmailAsync(emailRequest);
    }

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<EmailResponseDto> sendAppointmentConfirmationToPatientAsync(
            String patientEmail, String patientName, String doctorName, String appointmentDate, String appointmentTime) {
        log.debug("Processing appointment confirmation async to patient: {}", patientEmail);
        String subject = "Tu cita medica esta confirmada";
        String htmlContent = buildAppointmentConfirmationPatientHtml(patientName, doctorName, appointmentDate, appointmentTime);
        String textContent = buildAppointmentConfirmationPatientText(patientName, doctorName, appointmentDate, appointmentTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(patientEmail)
                .toName(patientName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmailAsync(emailRequest);
    }

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<EmailResponseDto> sendAppointmentConfirmationToDoctorAsync(
            String doctorEmail, String doctorName, String patientName, String appointmentDate, String appointmentTime) {
        log.debug("Processing appointment confirmation async to doctor: {}", doctorEmail);
        String subject = "Nueva cita programada en tu agenda";
        String htmlContent = buildAppointmentConfirmationDoctorHtml(doctorName, patientName, appointmentDate, appointmentTime);
        String textContent = buildAppointmentConfirmationDoctorText(doctorName, patientName, appointmentDate, appointmentTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(doctorEmail)
                .toName(doctorName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmailAsync(emailRequest);
    }

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<EmailResponseDto> sendAppointmentCancellationToPatientAsync(
            String patientEmail, String patientName, String doctorName, String appointmentDate, String appointmentTime) {
        log.debug("Processing appointment cancellation async to patient: {}", patientEmail);
        String subject = "Tu cita medica ha sido cancelada";
        String htmlContent = buildAppointmentCancellationPatientHtml(patientName, doctorName, appointmentDate, appointmentTime);
        String textContent = buildAppointmentCancellationPatientText(patientName, doctorName, appointmentDate, appointmentTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(patientEmail)
                .toName(patientName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmailAsync(emailRequest);
    }

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<EmailResponseDto> sendAppointmentCancellationToDoctorAsync(
            String doctorEmail, String doctorName, String patientName, String appointmentDate, String appointmentTime) {
        log.debug("Processing appointment cancellation async to doctor: {}", doctorEmail);
        String subject = "Cita cancelada en tu agenda";
        String htmlContent = buildAppointmentCancellationDoctorHtml(doctorName, patientName, appointmentDate, appointmentTime);
        String textContent = buildAppointmentCancellationDoctorText(doctorName, patientName, appointmentDate, appointmentTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(doctorEmail)
                .toName(doctorName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmailAsync(emailRequest);
    }

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<EmailResponseDto> sendAppointmentModificationApprovedToPatientAsync(
            String patientEmail, String patientName, String doctorName, String oldDate, String oldTime, String newDate, String newTime) {
        log.debug("Processing appointment modification approved async to patient: {}", patientEmail);
        String subject = "Tu solicitud de cambio de cita fue aprobada";
        String htmlContent = buildAppointmentModificationApprovedPatientHtml(patientName, doctorName, oldDate, oldTime, newDate, newTime);
        String textContent = buildAppointmentModificationApprovedPatientText(patientName, doctorName, oldDate, oldTime, newDate, newTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(patientEmail)
                .toName(patientName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmailAsync(emailRequest);
    }

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<EmailResponseDto> sendAppointmentModificationApprovedToDoctorAsync(
            String doctorEmail, String doctorName, String patientName, String oldDate, String oldTime, String newDate, String newTime) {
        log.debug("Processing appointment modification approved async to doctor: {}", doctorEmail);
        String subject = "Cambio de horario aprobado en tu agenda";
        String htmlContent = buildAppointmentModificationApprovedDoctorHtml(doctorName, patientName, oldDate, oldTime, newDate, newTime);
        String textContent = buildAppointmentModificationApprovedDoctorText(doctorName, patientName, oldDate, oldTime, newDate, newTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(doctorEmail)
                .toName(doctorName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmailAsync(emailRequest);
    }

    private String buildWelcomePatientHtml(String patientName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Bienvenido a MediBook</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Bienvenido a MediBook</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Hola %s,</h2>
                            
                            <p>Nos complace darte la bienvenida a MediBook, tu nueva plataforma de gestion medica.</p>
                            
                            <p>Con MediBook podras:</p>
                            <ul>
                                <li>Reservar citas medicas facilmente</li>
                                <li>Ver tu historial medico</li>
                                <li>Comunicarte con tus doctores</li>
                                <li>Gestionar tus turnos</li>
                            </ul>
                            
                            <p>Gracias por confiar en nosotros para el cuidado de tu salud.</p>
                            
                            <p>Saludos,<br>El equipo de MediBook</p>
                        </div>
                        
                        <div style="background-color: #f3f4f6; padding: 15px; text-align: center; border-radius: 8px; margin-top: 20px;">
                            <p style="margin: 0; font-size: 14px; color: #6b7280;">
                                Este es un mensaje automatico, por favor no responder a este email.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(patientName);
    }

    private String buildWelcomePatientText(String patientName) {
        return """
                Bienvenido a MediBook
                
                Hola %s,
                
                Nos complace darte la bienvenida a MediBook, tu nueva plataforma de gestion medica.
                
                Con MediBook podras:
                - Reservar citas medicas facilmente
                - Ver tu historial medico  
                - Comunicarte con tus doctores
                - Gestionar tus turnos
                
                Gracias por confiar en nosotros para el cuidado de tu salud.
                
                Saludos,
                El equipo de MediBook
                
                Este es un mensaje automatico, por favor no responder a este email.
                """.formatted(patientName);
    }

    private String buildDoctorApprovalHtml(String doctorName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Registro Aprobado</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #16a34a; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Registro Aprobado</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Felicitaciones Dr. %s,</h2>
                            
                            <p>Tu registro como medico en MediBook ha sido aprobado exitosamente.</p>
                            
                            <p>Ya puedes acceder a tu cuenta y comenzar a:</p>
                            <ul>
                                <li>Gestionar tu agenda medica</li>
                                <li>Atender pacientes</li>
                                <li>Acceder al historial de tus pacientes</li>
                                <li>Comunicarte con tu equipo</li>
                            </ul>
                            
                            <p>Bienvenido al equipo de profesionales de MediBook.</p>
                            
                            <p>Saludos,<br>El equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(doctorName);
    }

    private String buildDoctorApprovalText(String doctorName) {
        return """
                Registro Aprobado
                
                Felicitaciones Dr. %s,
                
                Tu registro como medico en MediBook ha sido aprobado exitosamente.
                
                Ya puedes acceder a tu cuenta y comenzar a:
                - Gestionar tu agenda medica
                - Atender pacientes
                - Acceder al historial de tus pacientes
                - Comunicarte con tu equipo
                
                Bienvenido al equipo de profesionales de MediBook.
                
                Saludos,
                El equipo de MediBook
                """.formatted(doctorName);
    }

    private String buildDoctorRejectionHtml(String doctorName, String reason) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Actualizacion de Registro</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #dc2626; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Actualizacion de Registro</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Estimado Dr. %s,</h2>
                            
                            <p>Lamentamos informarte que tu solicitud de registro como medico en MediBook no ha sido aprobada en esta ocasion.</p>
                            
                            <p><strong>Motivo:</strong> %s</p>
                            
                            <p>Si tienes preguntas o necesitas mas informacion, no dudes en contactarnos.</p>
                            
                            <p>Saludos,<br>El equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(doctorName, reason);
    }

    private String buildDoctorRejectionText(String doctorName, String reason) {
        return """
                Actualizacion de Registro
                
                Estimado Dr. %s,
                
                Lamentamos informarte que tu solicitud de registro como medico en MediBook no ha sido aprobada en esta ocasion.
                
                Motivo: %s
                
                Si tienes preguntas o necesitas mas informacion, no dudes en contactarnos.
                
                Saludos,
                El equipo de MediBook
                """.formatted(doctorName, reason);
    }

    private String buildAppointmentConfirmationPatientHtml(String patientName, String doctorName, String appointmentDate, String appointmentTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Cita Confirmada</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Cita Confirmada</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Hola %s,</h2>
                            
                            <p>Tu cita medica ha sido confirmada exitosamente.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Detalles de la cita:</h3>
                                <p><strong>Doctor:</strong> Dr. %s</p>
                                <p><strong>Fecha:</strong> %s</p>
                                <p><strong>Hora:</strong> %s</p>
                            </div>
                            
                            <p>Por favor, llega 15 minutos antes de tu cita.</p>
                            
                            <p>Saludos,<br>El equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(patientName, doctorName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentConfirmationPatientText(String patientName, String doctorName, String appointmentDate, String appointmentTime) {
        return """
                Cita Confirmada
                
                Hola %s,
                
                Tu cita medica ha sido confirmada exitosamente.
                
                Detalles de la cita:
                - Doctor: Dr. %s
                - Fecha: %s
                - Hora: %s
                
                Por favor, llega 15 minutos antes de tu cita.
                
                Saludos,
                El equipo de MediBook
                """.formatted(patientName, doctorName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentConfirmationDoctorHtml(String doctorName, String patientName, String appointmentDate, String appointmentTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Nueva Cita Programada</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Nueva Cita Programada</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Dr. %s,</h2>
                            
                            <p>Se ha programado una nueva cita en tu agenda.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Detalles de la cita:</h3>
                                <p><strong>Paciente:</strong> %s</p>
                                <p><strong>Fecha:</strong> %s</p>
                                <p><strong>Hora:</strong> %s</p>
                            </div>
                            
                            <p>Puedes revisar mas detalles en tu panel de control.</p>
                            
                            <p>Saludos,<br>El equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(doctorName, patientName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentConfirmationDoctorText(String doctorName, String patientName, String appointmentDate, String appointmentTime) {
        return """
                Nueva Cita Programada
                
                Dr. %s,
                
                Se ha programado una nueva cita en tu agenda.
                
                Detalles de la cita:
                - Paciente: %s
                - Fecha: %s
                - Hora: %s
                
                Puedes revisar mas detalles en tu panel de control.
                
                Saludos,
                El equipo de MediBook
                """.formatted(doctorName, patientName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentCancellationPatientHtml(String patientName, String doctorName, String appointmentDate, String appointmentTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Cita Cancelada</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #dc2626; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Cita Cancelada</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Hola %s,</h2>
                            
                            <p>Tu cita medica ha sido cancelada.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Detalles de la cita cancelada:</h3>
                                <p><strong>Doctor:</strong> Dr. %s</p>
                                <p><strong>Fecha:</strong> %s</p>
                                <p><strong>Hora:</strong> %s</p>
                            </div>
                            
                            <p>Puedes programar una nueva cita cuando lo desees.</p>
                            
                            <p>Saludos,<br>El equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(patientName, doctorName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentCancellationPatientText(String patientName, String doctorName, String appointmentDate, String appointmentTime) {
        return """
                Cita Cancelada
                
                Hola %s,
                
                Tu cita medica ha sido cancelada.
                
                Detalles de la cita cancelada:
                - Doctor: Dr. %s
                - Fecha: %s
                - Hora: %s
                
                Puedes programar una nueva cita cuando lo desees.
                
                Saludos,
                El equipo de MediBook
                """.formatted(patientName, doctorName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentCancellationDoctorHtml(String doctorName, String patientName, String appointmentDate, String appointmentTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Cita Cancelada</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #dc2626; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Cita Cancelada</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Dr. %s,</h2>
                            
                            <p>Una cita en tu agenda ha sido cancelada.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Detalles de la cita cancelada:</h3>
                                <p><strong>Paciente:</strong> %s</p>
                                <p><strong>Fecha:</strong> %s</p>
                                <p><strong>Hora:</strong> %s</p>
                            </div>
                            
                            <p>Tu agenda ha sido actualizada automaticamente.</p>
                            
                            <p>Saludos,<br>El equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(doctorName, patientName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentCancellationDoctorText(String doctorName, String patientName, String appointmentDate, String appointmentTime) {
        return """
                Cita Cancelada
                
                Dr. %s,
                
                Una cita en tu agenda ha sido cancelada.
                
                Detalles de la cita cancelada:
                - Paciente: %s
                - Fecha: %s
                - Hora: %s
                
                Tu agenda ha sido actualizada automaticamente.
                
                Saludos,
                El equipo de MediBook
                """.formatted(doctorName, patientName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentModificationApprovedPatientHtml(String patientName, String doctorName, String oldDate, String oldTime, String newDate, String newTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Cambio de Cita Aprobado</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #16a34a; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Cambio de Cita Aprobado</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Hola %s,</h2>
                            
                            <p>Tu solicitud de cambio de cita ha sido aprobada.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Cambio realizado:</h3>
                                <p><strong>Doctor:</strong> Dr. %s</p>
                                <p><strong>Fecha anterior:</strong> %s a las %s</p>
                                <p><strong>Nueva fecha:</strong> %s a las %s</p>
                            </div>
                            
                            <p>Por favor, llega 15 minutos antes de tu nueva cita.</p>
                            
                            <p>Saludos,<br>El equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(patientName, doctorName, oldDate, oldTime, newDate, newTime);
    }

    private String buildAppointmentModificationApprovedPatientText(String patientName, String doctorName, String oldDate, String oldTime, String newDate, String newTime) {
        return """
                Cambio de Cita Aprobado
                
                Hola %s,
                
                Tu solicitud de cambio de cita ha sido aprobada.
                
                Cambio realizado:
                - Doctor: Dr. %s
                - Fecha anterior: %s a las %s
                - Nueva fecha: %s a las %s
                
                Por favor, llega 15 minutos antes de tu nueva cita.
                
                Saludos,
                El equipo de MediBook
                """.formatted(patientName, doctorName, oldDate, oldTime, newDate, newTime);
    }

    private String buildAppointmentModificationApprovedDoctorHtml(String doctorName, String patientName, String oldDate, String oldTime, String newDate, String newTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Cambio de Horario Aprobado</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #16a34a; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Cambio de Horario Aprobado</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Dr. %s,</h2>
                            
                            <p>Se ha aprobado un cambio de horario en tu agenda.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Cambio realizado:</h3>
                                <p><strong>Paciente:</strong> %s</p>
                                <p><strong>Horario anterior:</strong> %s a las %s</p>
                                <p><strong>Nuevo horario:</strong> %s a las %s</p>
                            </div>
                            
                            <p>Tu agenda ha sido actualizada automaticamente.</p>
                            
                            <p>Saludos,<br>El equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(doctorName, patientName, oldDate, oldTime, newDate, newTime);
    }

    private String buildAppointmentModificationApprovedDoctorText(String doctorName, String patientName, String oldDate, String oldTime, String newDate, String newTime) {
        return """
                Cambio de Horario Aprobado
                
                Dr. %s,
                
                Se ha aprobado un cambio de horario en tu agenda.
                
                Cambio realizado:
                - Paciente: %s
                - Horario anterior: %s a las %s
                - Nuevo horario: %s a las %s
                
                Tu agenda ha sido actualizada automaticamente.
                
                Saludos,
                El equipo de MediBook
                """.formatted(doctorName, patientName, oldDate, oldTime, newDate, newTime);
    }
}