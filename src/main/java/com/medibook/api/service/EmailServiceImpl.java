package com.medibook.api.service;

import com.medibook.api.dto.email.EmailRequestDto;
import com.medibook.api.dto.email.EmailResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final GoogleAppsScriptEmailService googleAppsScriptEmailService;
    
    @Value("${email.enabled:false}")
    private boolean emailEnabled;

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
        value = {RuntimeException.class},
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
        
        if (emailRequest.getSubject() == null || emailRequest.getSubject().trim().isEmpty()) {
            log.error("Empty email subject for: {}", emailRequest.getTo());
            return EmailResponseDto.builder()
                    .success(false)
                    .message("Email subject is required")
                    .build();
        }
        
        // Enviar usando Google Apps Script
        log.debug("Using Google Apps Script for email delivery");
        EmailResponseDto result = googleAppsScriptEmailService.sendEmail(emailRequest);
        
        // Si Google Apps Script falla, lanzar excepción para activar el retry
        if (!result.isSuccess()) {
            throw new RuntimeException("Google Apps Script error: " + result.getMessage());
        }
        
        return result;
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
        String subject = "Registro confirmado en MediBook";
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
        String subject = "Registro médico aprobado";
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
        String subject = "Actualización sobre tu registro médico";
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
        String subject = "Confirmación de cita médica";
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
        String subject = "Nueva cita programada";
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
        String subject = "Cancelación de cita médica";
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
        String subject = "Cancelación de cita";
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
        String subject = "Modificación de cita médica aprobada";
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
        String subject = "Modificación de horario aprobada";
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
                    <title>Registro confirmado en MediBook</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Registro confirmado en MediBook</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Estimado/a %s,</h2>
                            
                            <p>Su registro en MediBook ha sido confirmado exitosamente.</p>
                            
                            <p>A través de nuestra plataforma podrá:</p>
                            <ul>
                                <li>Programar citas médicas</li>
                                <li>Consultar su historial médico</li>
                                <li>Acceder a información de sus consultas</li>
                                <li>Gestionar sus turnos médicos</li>
                            </ul>
                            
                            <p>Agradecemos su confianza en nuestros servicios.</p>
                            
                            <p>Atentamente,<br>Equipo de MediBook</p>
                        </div>
                        
                        <div style="background-color: #f3f4f6; padding: 15px; text-align: center; border-radius: 8px; margin-top: 20px;">
                            <p style="margin: 0; font-size: 14px; color: #6b7280;">
                                Este es un mensaje automático. Por favor, no responder a este email.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(patientName);
    }

    private String buildWelcomePatientText(String patientName) {
        return """
                Registro confirmado en MediBook
                
                Estimado/a %s,
                
                Su registro en MediBook ha sido confirmado exitosamente.
                
                A través de nuestra plataforma podrá:
                - Programar citas médicas
                - Consultar su historial médico
                - Acceder a información de sus consultas
                - Gestionar sus turnos médicos
                
                Agradecemos su confianza en nuestros servicios.
                
                Atentamente,
                Equipo de MediBook
                
                Este es un mensaje automático. Por favor, no responder a este email.
                """.formatted(patientName);
    }

    private String buildDoctorApprovalHtml(String doctorName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Registro Médico Aprobado</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #16a34a; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Registro Médico Aprobado</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Estimado/a Dr. %s,</h2>
                            
                            <p>Su registro como profesional médico en MediBook ha sido aprobado.</p>
                            
                            <p>A partir de ahora podrá acceder a las siguientes funcionalidades:</p>
                            <ul>
                                <li>Administrar su agenda médica</li>
                                <li>Gestionar consultas con pacientes</li>
                                <li>Acceder al historial clínico de sus pacientes</li>
                                <li>Utilizar las herramientas de comunicación</li>
                            </ul>
                            
                            <p>Le damos la bienvenida al equipo de profesionales de MediBook.</p>
                            
                            <p>Atentamente,<br>Equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(doctorName);
    }

    private String buildDoctorApprovalText(String doctorName) {
        return """
                Registro Médico Aprobado
                
                Estimado/a Dr. %s,
                
                Su registro como profesional médico en MediBook ha sido aprobado.
                
                A partir de ahora podrá acceder a las siguientes funcionalidades:
                - Administrar su agenda médica
                - Gestionar consultas con pacientes
                - Acceder al historial clínico de sus pacientes
                - Utilizar las herramientas de comunicación
                
                Le damos la bienvenida al equipo de profesionales de MediBook.
                
                Atentamente,
                Equipo de MediBook
                """.formatted(doctorName);
    }

    private String buildDoctorRejectionHtml(String doctorName, String reason) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Actualización de Registro Médico</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #dc2626; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Actualización de Registro Médico</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Estimado/a Dr. %s,</h2>
                            
                            <p>Le informamos que su solicitud de registro como profesional médico en MediBook no ha sido aprobada en esta oportunidad.</p>
                            
                            <p><strong>Motivo:</strong> %s</p>
                            
                            <p>Si requiere información adicional o desea presentar una nueva solicitud, puede contactarnos.</p>
                            
                            <p>Atentamente,<br>Equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(doctorName, reason);
    }

    private String buildDoctorRejectionText(String doctorName, String reason) {
        return """
                Actualización de Registro Médico
                
                Estimado/a Dr. %s,
                
                Le informamos que su solicitud de registro como profesional médico en MediBook no ha sido aprobada en esta oportunidad.
                
                Motivo: %s
                
                Si requiere información adicional o desea presentar una nueva solicitud, puede contactarnos.
                
                Atentamente,
                Equipo de MediBook
                """.formatted(doctorName, reason);
    }

    private String buildAppointmentConfirmationPatientHtml(String patientName, String doctorName, String appointmentDate, String appointmentTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Confirmación de Cita Médica</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Confirmación de Cita Médica</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Estimado/a %s,</h2>
                            
                            <p>Su cita médica ha sido confirmada.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Detalles de la cita:</h3>
                                <p><strong>Médico:</strong> Dr. %s</p>
                                <p><strong>Fecha:</strong> %s</p>
                                <p><strong>Hora:</strong> %s</p>
                            </div>
                            
                            <p>Le recomendamos presentarse 15 minutos antes del horario programado.</p>
                            
                            <p>Atentamente,<br>Equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(patientName, doctorName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentConfirmationPatientText(String patientName, String doctorName, String appointmentDate, String appointmentTime) {
        return """
                Confirmación de Cita Médica
                
                Estimado/a %s,
                
                Su cita médica ha sido confirmada.
                
                Detalles de la cita:
                - Médico: Dr. %s
                - Fecha: %s
                - Hora: %s
                
                Le recomendamos presentarse 15 minutos antes del horario programado.
                
                Atentamente,
                Equipo de MediBook
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
                            <h2>Estimado/a Dr. %s,</h2>
                            
                            <p>Se ha programado una nueva cita en su agenda.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Detalles de la cita:</h3>
                                <p><strong>Paciente:</strong> %s</p>
                                <p><strong>Fecha:</strong> %s</p>
                                <p><strong>Hora:</strong> %s</p>
                            </div>
                            
                            <p>Puede revisar información adicional en su panel de control.</p>
                            
                            <p>Atentamente,<br>Equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(doctorName, patientName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentConfirmationDoctorText(String doctorName, String patientName, String appointmentDate, String appointmentTime) {
        return """
                Nueva Cita Programada
                
                Estimado/a Dr. %s,
                
                Se ha programado una nueva cita en su agenda.
                
                Detalles de la cita:
                - Paciente: %s
                - Fecha: %s
                - Hora: %s
                
                Puede revisar información adicional en su panel de control.
                
                Atentamente,
                Equipo de MediBook
                """.formatted(doctorName, patientName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentCancellationPatientHtml(String patientName, String doctorName, String appointmentDate, String appointmentTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Cancelación de Cita Médica</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #dc2626; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Cancelación de Cita Médica</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Estimado/a %s,</h2>
                            
                            <p>Le informamos que su cita médica ha sido cancelada.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Detalles de la cita cancelada:</h3>
                                <p><strong>Médico:</strong> Dr. %s</p>
                                <p><strong>Fecha:</strong> %s</p>
                                <p><strong>Hora:</strong> %s</p>
                            </div>
                            
                            <p>Podrá programar una nueva cita cuando lo considere conveniente.</p>
                            
                            <p>Atentamente,<br>Equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(patientName, doctorName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentCancellationPatientText(String patientName, String doctorName, String appointmentDate, String appointmentTime) {
        return """
                Cancelación de Cita Médica
                
                Estimado/a %s,
                
                Le informamos que su cita médica ha sido cancelada.
                
                Detalles de la cita cancelada:
                - Médico: Dr. %s
                - Fecha: %s
                - Hora: %s
                
                Podrá programar una nueva cita cuando lo considere conveniente.
                
                Atentamente,
                Equipo de MediBook
                """.formatted(patientName, doctorName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentCancellationDoctorHtml(String doctorName, String patientName, String appointmentDate, String appointmentTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Cancelación de Cita</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #dc2626; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Cancelación de Cita</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Estimado/a Dr. %s,</h2>
                            
                            <p>Una cita en su agenda ha sido cancelada.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Detalles de la cita cancelada:</h3>
                                <p><strong>Paciente:</strong> %s</p>
                                <p><strong>Fecha:</strong> %s</p>
                                <p><strong>Hora:</strong> %s</p>
                            </div>
                            
                            <p>Su agenda ha sido actualizada automáticamente.</p>
                            
                            <p>Atentamente,<br>Equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(doctorName, patientName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentCancellationDoctorText(String doctorName, String patientName, String appointmentDate, String appointmentTime) {
        return """
                Cancelación de Cita
                
                Estimado/a Dr. %s,
                
                Una cita en su agenda ha sido cancelada.
                
                Detalles de la cita cancelada:
                - Paciente: %s
                - Fecha: %s
                - Hora: %s
                
                Su agenda ha sido actualizada automáticamente.
                
                Atentamente,
                Equipo de MediBook
                """.formatted(doctorName, patientName, appointmentDate, appointmentTime);
    }

    private String buildAppointmentModificationApprovedPatientHtml(String patientName, String doctorName, String oldDate, String oldTime, String newDate, String newTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Modificación de Cita Aprobada</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #16a34a; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Modificación de Cita Aprobada</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Estimado/a %s,</h2>
                            
                            <p>Su solicitud de modificación de cita ha sido aprobada.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Modificación realizada:</h3>
                                <p><strong>Médico:</strong> Dr. %s</p>
                                <p><strong>Fecha anterior:</strong> %s a las %s</p>
                                <p><strong>Nueva fecha:</strong> %s a las %s</p>
                            </div>
                            
                            <p>Le recomendamos presentarse 15 minutos antes del nuevo horario programado.</p>
                            
                            <p>Atentamente,<br>Equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(patientName, doctorName, oldDate, oldTime, newDate, newTime);
    }

    private String buildAppointmentModificationApprovedPatientText(String patientName, String doctorName, String oldDate, String oldTime, String newDate, String newTime) {
        return """
                Modificación de Cita Aprobada
                
                Estimado/a %s,
                
                Su solicitud de modificación de cita ha sido aprobada.
                
                Modificación realizada:
                - Médico: Dr. %s
                - Fecha anterior: %s a las %s
                - Nueva fecha: %s a las %s
                
                Le recomendamos presentarse 15 minutos antes del nuevo horario programado.
                
                Atentamente,
                Equipo de MediBook
                """.formatted(patientName, doctorName, oldDate, oldTime, newDate, newTime);
    }

    private String buildAppointmentModificationApprovedDoctorHtml(String doctorName, String patientName, String oldDate, String oldTime, String newDate, String newTime) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Modificación de Horario Aprobada</title>
                </head>
                <body>
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #16a34a; color: white; padding: 20px; text-align: center; border-radius: 8px;">
                            <h1>Modificación de Horario Aprobada</h1>
                        </div>
                        
                        <div style="padding: 30px 20px;">
                            <h2>Estimado/a Dr. %s,</h2>
                            
                            <p>Se ha aprobado una modificación de horario en su agenda.</p>
                            
                            <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                <h3>Modificación realizada:</h3>
                                <p><strong>Paciente:</strong> %s</p>
                                <p><strong>Horario anterior:</strong> %s a las %s</p>
                                <p><strong>Nuevo horario:</strong> %s a las %s</p>
                            </div>
                            
                            <p>Su agenda ha sido actualizada automáticamente.</p>
                            
                            <p>Atentamente,<br>Equipo de MediBook</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(doctorName, patientName, oldDate, oldTime, newDate, newTime);
    }

    private String buildAppointmentModificationApprovedDoctorText(String doctorName, String patientName, String oldDate, String oldTime, String newDate, String newTime) {
        return """
                Modificación de Horario Aprobada
                
                Estimado/a Dr. %s,
                
                Se ha aprobado una modificación de horario en su agenda.
                
                Modificación realizada:
                - Paciente: %s
                - Horario anterior: %s a las %s
                - Nuevo horario: %s a las %s
                
                Su agenda ha sido actualizada automáticamente.
                
                Atentamente,
                Equipo de MediBook
                """.formatted(doctorName, patientName, oldDate, oldTime, newDate, newTime);
    }
}