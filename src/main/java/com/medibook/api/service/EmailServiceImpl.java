package com.medibook.api.service;

import com.medibook.api.dto.Email.EmailRequestDto;
import com.medibook.api.dto.Email.EmailResponseDto;
import com.medibook.api.dto.Email.MailerSendEmailDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final WebClient mailerSendWebClient;
    
    @Value("${mailersend.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${mailersend.from.email}")
    private String fromEmail;
    
    @Value("${mailersend.from.name}")
    private String fromName;

    @Override
    public EmailResponseDto sendEmail(EmailRequestDto emailRequest) {
        
        if (!emailEnabled) {
            log.info("📧 EMAIL DESHABILITADO - Simulando envío a: {} | Asunto: {}", 
                    emailRequest.getTo(), emailRequest.getSubject());
            return EmailResponseDto.builder()
                    .success(true)
                    .messageId("dev-mode-" + System.currentTimeMillis())
                    .message("Email simulado en modo desarrollo")
                    .build();
        }
        
        try {
            MailerSendEmailDto mailerSendEmail = MailerSendEmailDto.builder()
                    .from(MailerSendEmailDto.From.builder()
                            .email(fromEmail)
                            .name(fromName)
                            .build())
                    .to(List.of(MailerSendEmailDto.To.builder()
                            .email(emailRequest.getTo())
                            .name(emailRequest.getToName())
                            .build()))
                    .subject(emailRequest.getSubject())
                    .html(emailRequest.getHtmlContent())
                    .text(emailRequest.getTextContent())
                    .build();
            
            try {
                mailerSendWebClient
                        .post()
                        .uri("/email")
                        .bodyValue(mailerSendEmail)
                        .exchangeToMono(clientResponse -> {
                            if (clientResponse.statusCode().is2xxSuccessful()) {
                                return clientResponse.releaseBody().then(reactor.core.publisher.Mono.just("OK"));
                            } else {
                                return clientResponse.bodyToMono(String.class)
                                        .map(body -> "Error " + clientResponse.statusCode() + ": " + body)
                                        .flatMap(error -> reactor.core.publisher.Mono.error(new RuntimeException(error)));
                            }
                        })
                        .block();
            } catch (Exception e) {
                throw new RuntimeException("Error al comunicarse con MailerSend: " + e.getMessage(), e);
            }
            
            return EmailResponseDto.builder()
                    .success(true)
                    .messageId("mailersend-" + System.currentTimeMillis())
                    .message("Email enviado exitosamente")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error al enviar email: {}", e.getMessage());
            
            return EmailResponseDto.builder()
                    .success(false)
                    .message("Error al enviar email")
                    .errorDetails(e.getMessage())
                    .build();
        }
    }

    @Override
    public EmailResponseDto sendWelcomeEmailToPatient(String patientEmail, String patientName) {
        String subject = "¡Bienvenido a MediBook!";
        String htmlContent = buildWelcomePatientHtml(patientName);
        String textContent = buildWelcomePatientText(patientName);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(patientEmail)
                .toName(patientName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmail(emailRequest);
    }

    @Override
    public EmailResponseDto sendApprovalEmailToDoctor(String doctorEmail, String doctorName) {
        String subject = "¡Tu registro como doctor ha sido aprobado!";
        String htmlContent = buildDoctorApprovalHtml(doctorName);
        String textContent = buildDoctorApprovalText(doctorName);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(doctorEmail)
                .toName(doctorName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmail(emailRequest);
    }

    @Override
    public EmailResponseDto sendRejectionEmailToDoctor(String doctorEmail, String doctorName, String reason) {
        String subject = "Actualización sobre tu registro como doctor";
        String htmlContent = buildDoctorRejectionHtml(doctorName, reason);
        String textContent = buildDoctorRejectionText(doctorName, reason);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(doctorEmail)
                .toName(doctorName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmail(emailRequest);
    }

    @Override
    public EmailResponseDto sendAppointmentConfirmationToPatient(String patientEmail, String patientName, 
                                                              String doctorName, String appointmentDate, String appointmentTime) {
        String subject = "Tu cita médica está confirmada";
        String htmlContent = buildAppointmentConfirmationPatientHtml(patientName, doctorName, appointmentDate, appointmentTime);
        String textContent = buildAppointmentConfirmationPatientText(patientName, doctorName, appointmentDate, appointmentTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(patientEmail)
                .toName(patientName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmail(emailRequest);
    }

    @Override
    public EmailResponseDto sendAppointmentConfirmationToDoctor(String doctorEmail, String doctorName, 
                                                              String patientName, String appointmentDate, String appointmentTime) {
        String subject = "📋 Nueva cita programada en tu agenda";
        String htmlContent = buildAppointmentConfirmationDoctorHtml(doctorName, patientName, appointmentDate, appointmentTime);
        String textContent = buildAppointmentConfirmationDoctorText(doctorName, patientName, appointmentDate, appointmentTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(doctorEmail)
                .toName(doctorName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmail(emailRequest);
    }

    @Override
    public EmailResponseDto sendAppointmentCancellationToPatient(String patientEmail, String patientName, 
                                                               String doctorName, String appointmentDate, String appointmentTime) {
        String subject = "Tu cita médica ha sido cancelada";
        String htmlContent = buildAppointmentCancellationPatientHtml(patientName, doctorName, appointmentDate, appointmentTime);
        String textContent = buildAppointmentCancellationPatientText(patientName, doctorName, appointmentDate, appointmentTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(patientEmail)
                .toName(patientName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmail(emailRequest);
    }

    @Override
    public EmailResponseDto sendAppointmentCancellationToDoctor(String doctorEmail, String doctorName, 
                                                              String patientName, String appointmentDate, String appointmentTime) {
        String subject = "📅 Cita cancelada en tu agenda";
        String htmlContent = buildAppointmentCancellationDoctorHtml(doctorName, patientName, appointmentDate, appointmentTime);
        String textContent = buildAppointmentCancellationDoctorText(doctorName, patientName, appointmentDate, appointmentTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(doctorEmail)
                .toName(doctorName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmail(emailRequest);
    }

    @Override
    public EmailResponseDto sendAppointmentModificationApprovedToPatient(String patientEmail, String patientName, 
                                                                       String doctorName, String oldDate, String oldTime,
                                                                       String newDate, String newTime) {
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
                
        return sendEmail(emailRequest);
    }

    @Override
    public EmailResponseDto sendAppointmentModificationApprovedToDoctor(String doctorEmail, String doctorName, 
                                                                       String patientName, String oldDate, String oldTime,
                                                                       String newDate, String newTime) {
        String subject = "📋 Cambio de horario aprobado en tu agenda";
        String htmlContent = buildAppointmentModificationApprovedDoctorHtml(doctorName, patientName, oldDate, oldTime, newDate, newTime);
        String textContent = buildAppointmentModificationApprovedDoctorText(doctorName, patientName, oldDate, oldTime, newDate, newTime);
        
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(doctorEmail)
                .toName(doctorName)
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .build();
                
        return sendEmail(emailRequest);
    }
    
    private String buildWelcomePatientHtml(String patientName) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2c5aa0;">¡Bienvenido a MediBook, %s!</h1>
                    <p>Con MediBook podrás:</p>
                    <ul>
                        <li>Agendar citas con doctores especializados</li>
                        <li>Ver tu historial de consultas</li>
                    </ul>
                    <p>¡Esperamos poder ayudarte con tus necesidades médicas!</p>
                    <br>
                    <p>Saludos,<br>El equipo de MediBook</p>
                </div>
            </body>
            </html>
            """, patientName);
    }
    
    private String buildWelcomePatientText(String patientName) {
        return String.format("""
            ¡Bienvenido a MediBook, %s!
                        
            Con MediBook podrás:
            - Agendar citas con doctores especializados
            - Ver tu historial de consultas
            
            Si tienes alguna pregunta, no dudes en contactarnos.
                        
            Saludos,
            El equipo de MediBook
            """, patientName);
    }
    
    private String buildDoctorApprovalHtml(String doctorName) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #28a745;">¡Felicitaciones, Dr. %s!</h1>
                    <p>Tu registro como doctor en MediBook ha sido <strong>aprobado</strong>.</p>
                    <p>Ya puedes acceder a tu cuenta y comenzar a:</p>
                    <ul>
                        <li>Gestionar tu disponibilidad</li>
                        <li>Ver tus citas programadas</li>
                        <li>Acceder al historial médico de tus pacientes</li>
                        <li>Actualizar tu perfil profesional</li>
                    </ul>
                    <p>Te damos la bienvenida al equipo de profesionales de MediBook.</p>
                    <br>
                    <p>Saludos,<br>El equipo de MediBook</p>
                </div>
            </body>
            </html>
            """, doctorName);
    }
    
    private String buildDoctorApprovalText(String doctorName) {
        return String.format("""
            ¡Felicitaciones, Dr. %s!
            
            Tu registro como doctor en MediBook ha sido aprobado.
            
            Ya puedes acceder a tu cuenta y comenzar a:
            - Gestionar tu disponibilidad
            - Ver tus citas programadas
            - Acceder al historial médico de tus pacientes
            - Actualizar tu perfil profesional
            
            Te damos la bienvenida al equipo de profesionales de MediBook.
            
            Saludos,
            El equipo de MediBook
            """, doctorName);
    }
    
    private String buildDoctorRejectionHtml(String doctorName, String reason) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #dc3545;">Actualización sobre tu registro</h1>
                    <p>Estimado/a Dr. %s,</p>
                    <p>Lamentamos informarte que tu solicitud de registro como doctor en MediBook no ha sido aprobada en esta ocasión.</p>
                    <p><strong>Motivo:</strong> %s</p>
                    <p>Gracias por tu interés en formar parte de MediBook.</p>
                    <br>
                    <p>Saludos,<br>El equipo de MediBook</p>
                </div>
            </body>
            </html>
            """, doctorName, reason);
    }
    
    private String buildDoctorRejectionText(String doctorName, String reason) {
        return String.format("""
            Actualización sobre tu registro
            
            Estimado/a Dr. %s,
            
            Lamentamos informarte que tu solicitud de registro como doctor en MediBook no ha sido aprobada en esta ocasión.
            
            Motivo: %s
                        
            Gracias por tu interés en formar parte de MediBook.
            
            Saludos,
            El equipo de MediBook
            """, doctorName, reason);
    }
    
    // ===== MÉTODOS ESPECÍFICOS PARA PACIENTES =====
    
    private String buildAppointmentConfirmationPatientHtml(String patientName, String doctorName, 
                                                          String appointmentDate, String appointmentTime) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #28a745;">¡Tu cita está confirmada!</h1>
                    <p>Hola %s,</p>
                    <p>¡Excelente! Tu cita médica ha sido confirmada exitosamente.</p>
                    
                    <div style="background-color: #d4edda; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
                        <h3 style="color: #155724; margin-top: 0;">📅 Detalles de tu cita:</h3>
                        <p style="margin: 8px 0; font-size: 16px;"><strong>🩺 Doctor:</strong> %s</p>
                        <p style="margin: 8px 0; font-size: 16px;"><strong>📅 Fecha:</strong> %s</p>
                        <p style="margin: 8px 0; font-size: 16px;"><strong>🕐 Hora:</strong> %s</p>
                    </div>
                    
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107;">
                        <h4 style="color: #856404; margin-top: 0;">Recordatorios importantes:</h4>
                        <ul style="color: #856404; margin: 10px 0;">
                            <li>Llega <strong>10 minutos antes</strong> de tu cita</li>
                            <li>Trae tu documento de identidad</li>
                            <li>Si necesitas cancelar, hazlo con anticipación</li>
                        </ul>
                    </div>
                    
                    <br>
                    <p>Saludos,<br>El equipo de MediBook</p>
                </div>
            </body>
            </html>
            """, patientName, doctorName, appointmentDate, appointmentTime);
    }
    
    private String buildAppointmentConfirmationPatientText(String patientName, String doctorName, 
                                                          String appointmentDate, String appointmentTime) {
        return String.format("""
            ¡Tu cita está confirmada!
            
            Hola %s,
            
            ¡Excelente! Tu cita médica ha sido confirmada exitosamente.
            
            📅 DETALLES DE TU CITA:
            🩺 Doctor: %s
            📅 Fecha: %s
            🕐 Hora: %s
            
            RECORDATORIOS IMPORTANTES:
            • Llega 10 minutos antes de tu cita
            • Trae tu documento de identidad
            • Si necesitas cancelar, hazlo con anticipación
            
            
            Saludos,
            El equipo de MediBook
            """, patientName, doctorName, appointmentDate, appointmentTime);
    }
    
    private String buildAppointmentCancellationPatientHtml(String patientName, String doctorName, 
                                                          String appointmentDate, String appointmentTime) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #dc3545;">Tu cita ha sido cancelada</h1>
                    <p>Hola %s,</p>
                    <p>Lamentamos informarte que tu cita médica ha sido cancelada.</p>
                    
                    <div style="background-color: #f8d7da; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                        <h3 style="color: #721c24; margin-top: 0;">📅 Cita cancelada:</h3>
                        <p style="margin: 8px 0;"><strong>🩺 Doctor:</strong> %s</p>
                        <p style="margin: 8px 0;"><strong>📅 Fecha:</strong> %s</p>
                        <p style="margin: 8px 0;"><strong>🕐 Hora:</strong> %s</p>
                    </div>
                    
                    <div style="background-color: #d1ecf1; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #17a2b8;">
                        <h4 style="color: #0c5460; margin-top: 0;">💡 ¿Qué puedes hacer ahora?</h4>
                        <ul style="color: #0c5460; margin: 10px 0;">
                            <li>Agenda una nueva cita cuando gustes</li>
                            <li>Consulta otros doctores disponibles</li>
                        </ul>
                    </div>
                    
                    <br>
                    <p>Saludos,<br>El equipo de MediBook</p>
                </div>
            </body>
            </html>
            """, patientName, doctorName, appointmentDate, appointmentTime);
    }
    
    private String buildAppointmentCancellationPatientText(String patientName, String doctorName, 
                                                          String appointmentDate, String appointmentTime) {
        return String.format("""
            Tu cita ha sido cancelada
            
            Hola %s,
            
            Lamentamos informarte que tu cita médica ha sido cancelada.
            
            📅 CITA CANCELADA:
            🩺 Doctor: %s
            📅 Fecha: %s
            🕐 Hora: %s
            
            💡 ¿QUÉ PUEDES HACER AHORA?
            • Agenda una nueva cita cuando gustes
            • Consulta otros doctores disponibles
                        
            Saludos,
            El equipo de MediBook
            """, patientName, doctorName, appointmentDate, appointmentTime);
    }
    
    private String buildAppointmentModificationApprovedPatientHtml(String patientName, String doctorName,
                                                                  String oldDate, String oldTime, String newDate, String newTime) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #28a745;">¡Tu cambio de cita fue aprobado!</h1>
                    <p>Hola %s,</p>
                    <p>¡Excelente noticia! Tu solicitud de cambio de horario ha sido <strong>aprobada</strong> por el doctor.</p>
                    
                    <div style="background-color: #f8d7da; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                        <h3 style="color: #721c24; margin-top: 0;">Cita anterior (cancelada):</h3>
                        <p style="margin: 5px 0;"><strong>🩺 Doctor:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>📅 Fecha:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>🕐 Hora:</strong> %s</p>
                    </div>
                    
                    <div style="background-color: #d4edda; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
                        <h3 style="color: #155724; margin-top: 0;">Tu nueva cita confirmada:</h3>
                        <p style="margin: 5px 0;"><strong>🩺 Doctor:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>📅 Fecha:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>🕐 Hora:</strong> %s</p>
                    </div>
                    
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107;">
                        <h4 style="color: #856404; margin-top: 0;">Recordatorios:</h4>
                        <ul style="color: #856404; margin: 10px 0;">
                            <li>Llega 10 minutos antes de tu nueva cita</li>
                            <li>Agenda tu nueva cita en tu calendario personal</li>
                            <li>Si necesitas otro cambio, solicítalo con anticipación</li>
                        </ul>
                    </div>
                    
                    <br>
                    <p>Saludos,<br>El equipo de MediBook</p>
                </div>
            </body>
            </html>
            """, patientName, doctorName, oldDate, oldTime, doctorName, newDate, newTime);
    }
    
    private String buildAppointmentModificationApprovedPatientText(String patientName, String doctorName,
                                                                  String oldDate, String oldTime, String newDate, String newTime) {
        return String.format("""
            ¡Tu cambio de cita fue aprobado!
            
            Hola %s,
            
            ¡Excelente noticia! Tu solicitud de cambio de horario ha sido APROBADA por el doctor.
            
            CITA ANTERIOR (CANCELADA):
            🩺 Doctor: %s
            📅 Fecha: %s
            🕐 Hora: %s
            
            TU NUEVA CITA CONFIRMADA:
            🩺 Doctor: %s
            📅 Fecha: %s
            🕐 Hora: %s
            
            RECORDATORIOS:
            • Llega 10 minutos antes de tu nueva cita
            • Agenda tu nueva cita en tu calendario personal
            • Si necesitas otro cambio, solicítalo con anticipación
            
            
            Saludos,
            El equipo de MediBook
            """, patientName, doctorName, oldDate, oldTime, doctorName, newDate, newTime);
    }
    
    // ===== MÉTODOS ESPECÍFICOS PARA DOCTORES =====
    
    private String buildAppointmentConfirmationDoctorHtml(String doctorName, String patientName, 
                                                         String appointmentDate, String appointmentTime) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2c5aa0;">📋 Nueva cita en tu agenda</h1>
                    <p>Dr. %s,</p>
                    <p>Se ha programado una nueva cita en tu agenda médica.</p>
                    
                    <div style="background-color: #e7f3ff; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2c5aa0;">
                        <h3 style="color: #1e3a5f; margin-top: 0;">📅 Detalles de la consulta:</h3>
                        <p style="margin: 8px 0; font-size: 16px;"><strong>👤 Paciente:</strong> %s</p>
                        <p style="margin: 8px 0; font-size: 16px;"><strong>📅 Fecha:</strong> %s</p>
                        <p style="margin: 8px 0; font-size: 16px;"><strong>🕐 Hora:</strong> %s</p>
                    </div>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #6c757d;">
                        <h4 style="color: #495057; margin-top: 0;">Recordatorios:</h4>
                        <ul style="color: #495057; margin: 10px 0;">
                            <li>Revisar el historial médico del paciente antes de la consulta</li>
                            <li>Verificar disponibilidad de equipos médicos necesarios</li>
                            <li>El paciente ha sido notificado de la cita</li>
                        </ul>
                    </div>
                    
                    <br>
                    <p>Saludos,<br>El equipo de MediBook</p>
                </div>
            </body>
            </html>
            """, doctorName, patientName, appointmentDate, appointmentTime);
    }
    
    private String buildAppointmentConfirmationDoctorText(String doctorName, String patientName, 
                                                         String appointmentDate, String appointmentTime) {
        return String.format("""
            📋 Nueva cita en tu agenda
            
            Dr. %s,
            
            Se ha programado una nueva cita en tu agenda médica.
            
            📅 DETALLES DE LA CONSULTA:
            👤 Paciente: %s
            📅 Fecha: %s
            🕐 Hora: %s
            
            RECORDATORIOS:
            • Revisar el historial médico del paciente antes de la consulta
            • Verificar disponibilidad de equipos médicos necesarios
            • El paciente ha sido notificado de la cita
            
            
            Saludos,
            El equipo de MediBook
            """, doctorName, patientName, appointmentDate, appointmentTime);
    }
    
    private String buildAppointmentCancellationDoctorHtml(String doctorName, String patientName, 
                                                         String appointmentDate, String appointmentTime) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #dc3545;">📅 Cita cancelada en tu agenda</h1>
                    <p>Dr. %s,</p>
                    <p>Te informamos que una cita ha sido cancelada en tu agenda médica.</p>
                    
                    <div style="background-color: #f8d7da; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                        <h3 style="color: #721c24; margin-top: 0;">📅 Cita cancelada:</h3>
                        <p style="margin: 8px 0;"><strong>👤 Paciente:</strong> %s</p>
                        <p style="margin: 8px 0;"><strong>📅 Fecha:</strong> %s</p>
                        <p style="margin: 8px 0;"><strong>🕐 Hora:</strong> %s</p>
                    </div>
                    
                    <div style="background-color: #d1ecf1; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #17a2b8;">
                        <ul style="color: #0c5460; margin: 10px 0;">
                            <li>Tu horario ahora está disponible para otros pacientes</li>
                            <li>El paciente ha sido notificado de la cancelación</li>
                            <li>Puedes revisar tu agenda actualizada en la plataforma</li>
                        </ul>
                    </div>
                    
                    <br>
                    <p>Saludos,<br>El equipo de MediBook</p>
                </div>
            </body>
            </html>
            """, doctorName, patientName, appointmentDate, appointmentTime);
    }
    
    private String buildAppointmentCancellationDoctorText(String doctorName, String patientName, 
                                                         String appointmentDate, String appointmentTime) {
        return String.format("""
            📅 Cita cancelada en tu agenda
            
            Dr. %s,
            
            Te informamos que una cita ha sido cancelada en tu agenda médica.
            
            📅 CITA CANCELADA:
            👤 Paciente: %s
            📅 Fecha: %s
            🕐 Hora: %s
            
            • Tu horario ahora está disponible para otros pacientes
            • El paciente ha sido notificado de la cancelación
            • Puedes revisar tu agenda actualizada en la plataforma
            
            
            Saludos,
            El equipo de MediBook
            """, doctorName, patientName, appointmentDate, appointmentTime);
    }
    
    private String buildAppointmentModificationApprovedDoctorHtml(String doctorName, String patientName,
                                                                 String oldDate, String oldTime, String newDate, String newTime) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #28a745;">📋 Cambio de horario confirmado en tu agenda</h1>
                    <p>Dr. %s,</p>
                    <p>Se ha actualizado tu agenda médica con el cambio de horario que aprobaste.</p>
                    
                    <div style="background-color: #f8d7da; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                        <h3 style="color: #721c24; margin-top: 0;">Horario anterior (liberado):</h3>
                        <p style="margin: 5px 0;"><strong>👤 Paciente:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>📅 Fecha:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>🕐 Hora:</strong> %s</p>
                    </div>
                    
                    <div style="background-color: #d4edda; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
                        <h3 style="color: #155724; margin-top: 0;">Nuevo horario confirmado:</h3>
                        <p style="margin: 5px 0;"><strong>👤 Paciente:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>📅 Fecha:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>🕐 Hora:</strong> %s</p>
                    </div>
                    
                    <div style="background-color: #e7f3ff; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2c5aa0;">
                        <ul style="color: #1e3a5f; margin: 10px 0;">
                            <li>Tu agenda ha sido actualizada automáticamente</li>
                            <li>El paciente ha sido notificado del cambio aprobado</li>
                            <li>El horario anterior ahora está disponible para otros pacientes</li>
                        </ul>
                    </div>
                    
                    <br>
                    <p>Saludos,<br>El equipo de MediBook</p>
                </div>
            </body>
            </html>
            """, doctorName, patientName, oldDate, oldTime, patientName, newDate, newTime);
    }
    
    private String buildAppointmentModificationApprovedDoctorText(String doctorName, String patientName,
                                                                 String oldDate, String oldTime, String newDate, String newTime) {
        return String.format("""
            📋 Cambio de horario confirmado en tu agenda
            
            Dr. %s,
            
            Se ha actualizado tu agenda médica con el cambio de horario que aprobaste.
            
            HORARIO ANTERIOR (LIBERADO):
            👤 Paciente: %s
            📅 Fecha: %s
            🕐 Hora: %s
            
            NUEVO HORARIO CONFIRMADO:
            👤 Paciente: %s
            📅 Fecha: %s
            🕐 Hora: %s
            
            • Tu agenda ha sido actualizada automáticamente
            • El paciente ha sido notificado del cambio aprobado
            • El horario anterior ahora está disponible para otros pacientes
            
            
            Saludos,
            El equipo de MediBook
            """, doctorName, patientName, oldDate, oldTime, patientName, newDate, newTime);
    }
}