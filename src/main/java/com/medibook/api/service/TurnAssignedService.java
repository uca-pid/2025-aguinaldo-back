package com.medibook.api.service;

import com.medibook.api.dto.Turn.TurnCreateRequestDTO;
import com.medibook.api.dto.Turn.TurnResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.TurnAssignedMapper;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.UserRepository;
import com.medibook.api.util.DateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TurnAssignedService {

    private final TurnAssignedRepository turnRepo;
    private final UserRepository userRepo;
    private final TurnAssignedMapper mapper;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final TurnFileService turnFileService;
    private static final ZoneId ARGENTINA_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    public TurnResponseDTO createTurn(TurnCreateRequestDTO dto) {
        User doctor = userRepo.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }
        
        if (!"ACTIVE".equals(doctor.getStatus())) {
            throw new RuntimeException("Doctor is not active");
        }
        
        User patient = userRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!"PATIENT".equals(patient.getRole())) {
            throw new RuntimeException("User is not a patient");
        }
        
        if (!"ACTIVE".equals(patient.getStatus())) {
            throw new RuntimeException("Patient is not active");
        }

        boolean slotTaken = turnRepo.existsByDoctor_IdAndScheduledAtAndStatusNotCancelled(
                dto.getDoctorId(), dto.getScheduledAt());
        
        if (slotTaken) {
            throw new RuntimeException("Time slot is already taken");
        }

        TurnAssigned turn = TurnAssigned.builder()
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(dto.getScheduledAt())
                .status("SCHEDULED")
                .build();

        TurnAssigned saved = turnRepo.save(turn);
        
        try {
            String date = DateTimeUtils.formatDate(saved.getScheduledAt());
            String time = DateTimeUtils.formatTime(saved.getScheduledAt());
            
            
            final String patientEmail = patient.getEmail();
            final String patientName = patient.getName();
            final String doctorEmail = doctor.getEmail();
            final String doctorName = doctor.getName();
            
            
            emailService.sendAppointmentConfirmationToPatientAsync(
                patientEmail, 
                patientName, 
                doctorName, 
                date, 
                time
            ).thenAccept(response -> {
                if (response.isSuccess()) {
                    log.info("Confirmación enviada al paciente: {}", patientEmail);
                } else {
                    log.warn("Falló confirmación al paciente {}: {}", patientEmail, response.getMessage());
                }
            });
            
            emailService.sendAppointmentConfirmationToDoctorAsync(
                doctorEmail, 
                doctorName, 
                patientName, 
                date, 
                time
            ).thenAccept(response -> {
                if (response.isSuccess()) {
                    log.info("Confirmación enviada al doctor: {}", doctorEmail);
                } else {
                    log.warn("Falló confirmación al doctor {}: {}", doctorEmail, response.getMessage());
                }
            });
            
            log.info("Emails de confirmación de cita encolados para paciente {} y doctor {}", 
                    patientEmail, doctorEmail);
            
        } catch (Exception e) {
            log.warn("Error encolando emails de confirmación de cita: {}", e.getMessage());
        }

        // Create notification for the doctor
        try {
            String dateFormatted = DateTimeUtils.formatDate(saved.getScheduledAt());
            String timeFormatted = DateTimeUtils.formatTime(saved.getScheduledAt());
            String patientFullName = patient.getName() + " " + patient.getSurname();

            notificationService.createTurnReservedNotification(
                    saved.getDoctor().getId(),
                    saved.getId(),
                    patientFullName,
                    dateFormatted,
                    timeFormatted
            );

            log.info("Notification created for doctor {} about turn creation by patient {}", 
                    saved.getDoctor().getId(), patient.getId());
        } catch (Exception e) {
            log.error("Error creating notification for turn creation: {}", e.getMessage());
            // Don't fail the creation if notification fails
        }
        
        return mapper.toDTO(saved);
    }

    public TurnAssigned reserveTurn(UUID turnId, UUID patientId) {
        TurnAssigned turn = turnRepo.findById(turnId)
                .orElseThrow(() -> new RuntimeException("Turn not found"));

        if (!"AVAILABLE".equals(turn.getStatus())) {
            throw new RuntimeException("Turn is not available");
        }

        User patient = userRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        turn.setPatient(patient);
        turn.setStatus("RESERVED");

        return turnRepo.save(turn);
    }
    
    public List<TurnResponseDTO> getTurnsByDoctor(UUID doctorId) {
        List<TurnAssigned> turns = turnRepo.findByDoctor_IdOrderByScheduledAtDesc(doctorId);
        return turns.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<TurnResponseDTO> getTurnsByPatient(UUID patientId) {
        List<TurnAssigned> turns = turnRepo.findByPatient_IdOrderByScheduledAtDesc(patientId);
        return turns.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<TurnResponseDTO> getTurnsByDoctorAndStatus(UUID doctorId, String status) {
        List<TurnAssigned> turns = turnRepo.findByDoctor_IdAndStatusOrderByScheduledAtDesc(doctorId, status);
        return turns.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<TurnResponseDTO> getTurnsByPatientAndStatus(UUID patientId, String status) {
        List<TurnAssigned> turns = turnRepo.findByPatient_IdAndStatusOrderByScheduledAtDesc(patientId, status);
        return turns.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    private final com.medibook.api.repository.TurnModifyRequestRepository turnModifyRequestRepository;

    public TurnResponseDTO cancelTurn(UUID turnId, UUID userId, String userRole) {
        TurnAssigned turn = turnRepo.findById(turnId)
                .orElseThrow(() -> new RuntimeException("Turn not found"));
        
        if ("PATIENT".equals(userRole)) {
            if (turn.getPatient() == null || !turn.getPatient().getId().equals(userId)) {
                throw new RuntimeException("You can only cancel your own turns");
            }
        } else if ("DOCTOR".equals(userRole)) {
            if (turn.getDoctor() == null || !turn.getDoctor().getId().equals(userId)) {
                throw new RuntimeException("You can only cancel your own turns");
            }
        } else {
            throw new RuntimeException("Invalid user role for cancellation");
        }
        
        if (!"SCHEDULED".equals(turn.getStatus()) && !"RESERVED".equals(turn.getStatus())) {
            throw new RuntimeException("Turn cannot be canceled. Current status: " + turn.getStatus());
        }
        
        OffsetDateTime now = OffsetDateTime.now(ARGENTINA_ZONE);
        if (turn.getScheduledAt().isBefore(now)) {
            throw new RuntimeException("Cannot cancel past turns");
        }
        
        turn.setStatus("CANCELED");
        TurnAssigned saved = turnRepo.save(turn);

        
        try {
            if (turnFileService.fileExistsForTurn(turnId)) {
                log.info("Deleting file associated with canceled turn: {}", turnId);
                turnFileService.deleteTurnFile(turnId).block(); 
                log.info("File successfully deleted for canceled turn: {}", turnId);
            }
        } catch (Exception e) {
            log.warn("Failed to delete file for canceled turn {}: {}", turnId, e.getMessage());
        }

        try {
            String date = DateTimeUtils.formatDate(saved.getScheduledAt());
            String time = DateTimeUtils.formatTime(saved.getScheduledAt());
            
            
            final String patientEmail = turn.getPatient().getEmail();
            final String patientName = turn.getPatient().getName();
            final String doctorEmail = turn.getDoctor().getEmail();
            final String doctorName = turn.getDoctor().getName();
            
            
            emailService.sendAppointmentCancellationToPatientAsync(
                patientEmail,
                patientName,
                doctorName,
                date,
                time
            ).thenAccept(response -> {
                if (response.isSuccess()) {
                    log.info("Email de cancelación enviado al paciente: {}", patientEmail);
                } else {
                    log.warn("Falló email de cancelación al paciente {}: {}", patientEmail, response.getMessage());
                }
            });
            
            emailService.sendAppointmentCancellationToDoctorAsync(
                doctorEmail,
                doctorName,
                patientName,
                date,
                time
            ).thenAccept(response -> {
                if (response.isSuccess()) {
                    log.info("Email de cancelación enviado al doctor: {}", doctorEmail);
                } else {
                    log.warn("Falló email de cancelación al doctor {}: {}", doctorEmail, response.getMessage());
                }
            });
            
            log.info("Emails de cancelación encolados para paciente {} y doctor {}", 
                    patientEmail, doctorEmail);
            
        } catch (Exception e) {
            log.warn("Error encolando emails de cancelación: {}", e.getMessage());
        }

        UUID notificationUserId;
        String cancelledBy;
        if ("PATIENT".equals(userRole)) {
            notificationUserId = turn.getDoctor().getId();
            cancelledBy = "patient";
        } else {
            notificationUserId = turn.getPatient().getId();
            cancelledBy = "doctor";
        }
        
        String dateForNotification = DateTimeUtils.formatDate(saved.getScheduledAt());
        String timeForNotification = DateTimeUtils.formatTime(saved.getScheduledAt());
        
        notificationService.createTurnCancellationNotification(
            notificationUserId, 
            turnId, 
            cancelledBy,
            turn.getDoctor().getName() + " " + turn.getDoctor().getSurname(),
            turn.getPatient().getName() + " " + turn.getPatient().getSurname(),
            dateForNotification,
            timeForNotification
        );

        boolean hasPendingRequest = turnModifyRequestRepository.findByTurnAssigned_IdAndStatus(turnId, "PENDING").isPresent();
        if (hasPendingRequest) {
            turnModifyRequestRepository.deleteByTurnAssigned_IdAndStatus(turnId, "PENDING");
        }

        return mapper.toDTO(saved);
    }
}
