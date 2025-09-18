package com.medibook.api.controller;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.medibook.api.dto.Turn.TurnCreateRequestDTO;
import com.medibook.api.dto.Turn.TurnReserveRequestDTO;
import com.medibook.api.dto.Turn.TurnResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.dto.Availability.AvailableSlotDTO;
import com.medibook.api.service.DoctorAvailabilityService;
import com.medibook.api.service.TurnAssignedService;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.util.AuthorizationUtil;
import com.medibook.api.util.TurnAuthorizationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/turns")
@RequiredArgsConstructor
@Slf4j
public class TurnAssignedController {

    private final TurnAssignedService turnService;
    private final DoctorAvailabilityService doctorAvailabilityService;
    private final TurnAssignedRepository turnAssignedRepository;

    @PostMapping
    public ResponseEntity<Object> createTurn(
            @Valid @RequestBody TurnCreateRequestDTO dto, 
            HttpServletRequest request) {
        
        log.info("Received turn creation request");
        log.info("Request DTO: {}", dto);
        log.info("DoctorId: {}", dto.getDoctorId());
        log.info("PatientId: {}", dto.getPatientId()); 
        log.info("ScheduledAt: {}", dto.getScheduledAt());
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        log.info("Authenticated user: {} (Role: {})", 
                authenticatedUser != null ? authenticatedUser.getId() : "null",
                authenticatedUser != null ? authenticatedUser.getRole() : "null");
        
        if (AuthorizationUtil.isPatient(authenticatedUser)) {
            log.info("User is patient, validating patient turn creation");
            ResponseEntity<Object> validationError = TurnAuthorizationUtil.validatePatientTurnCreation(authenticatedUser, dto.getPatientId());
            if (validationError != null) {
                log.warn("Patient turn creation validation failed");
                return validationError;
            }
        } else if (AuthorizationUtil.isDoctor(authenticatedUser)) {
            log.info("User is doctor, validating doctor turn creation");
            ResponseEntity<Object> validationError = TurnAuthorizationUtil.validateDoctorTurnCreation(authenticatedUser, dto.getDoctorId());
            if (validationError != null) {
                log.warn("Doctor turn creation validation failed");
                return validationError;
            }
        } else {
            log.warn("User has invalid role for turn creation");
            return AuthorizationUtil.createInvalidRoleResponse();
        }
        
        log.info("Calling turnService.createTurn with DTO: {}", dto);
        TurnResponseDTO result = turnService.createTurn(dto);
        log.info("Turn created successfully: {}", result);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/available")
    public ResponseEntity<List<OffsetDateTime>> getAvailableTurns(
            @RequestParam UUID doctorId,
            @RequestParam String date,
            HttpServletRequest request) {
        
        log.info("Getting available turns - Doctor ID: {}, Date: {}", doctorId, date);
        
        LocalDate localDate = LocalDate.parse(date);
        
        // Obtener los slots disponibles del doctor para esta fecha específica
        List<AvailableSlotDTO> availableSlots = doctorAvailabilityService.getAvailableSlots(
                doctorId, localDate, localDate);
        
        log.info("Found {} configured slots for doctor on {}", availableSlots.size(), localDate);
        
        if (availableSlots.isEmpty()) {
            log.warn("No availability configured for doctor {} on {}", doctorId, localDate);
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        // Convertir los slots a OffsetDateTime y filtrar los que están ocupados
        List<OffsetDateTime> availableTimes = new ArrayList<>();
        ZoneOffset argentinaOffset = ZoneOffset.of("-03:00");
        
        for (AvailableSlotDTO slot : availableSlots) {
            // Combinar fecha del slot con su hora de inicio
            OffsetDateTime slotDateTime = slot.getDate().atTime(slot.getStartTime()).atOffset(argentinaOffset);
            
            // Verificar si este horario específico ya está ocupado usando el repositorio
            boolean isOccupied = turnAssignedRepository.existsByDoctor_IdAndScheduledAt(doctorId, slotDateTime);
            
            if (!isOccupied) {
                availableTimes.add(slotDateTime);
            } else {
                log.info("Slot {} is already occupied for doctor {}", slotDateTime, doctorId);
            }
        }
        
        log.info("Found {} available time slots after filtering occupied turns", availableTimes.size());
        log.info("Available slots: {}", availableTimes);
        
        return ResponseEntity.ok(availableTimes);
    }

    @PostMapping("/reserve")
    public ResponseEntity<Object> reserveTurn(
            @RequestBody TurnReserveRequestDTO dto,
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        ResponseEntity<Object> validationError = TurnAuthorizationUtil.validatePatientTurnReservation(authenticatedUser, dto.getPatientId());
        if (validationError != null) {
            return validationError;
        }
        
        TurnAssigned result = turnService.reserveTurn(dto.getTurnId(), dto.getPatientId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-turns")
    public ResponseEntity<Object> getMyTurns(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        List<TurnResponseDTO> turns;
        
        if (AuthorizationUtil.isDoctor(authenticatedUser)) {
            if (status != null && !status.isEmpty()) {
                turns = turnService.getTurnsByDoctorAndStatus(authenticatedUser.getId(), status);
            } else {
                turns = turnService.getTurnsByDoctor(authenticatedUser.getId());
            }
        } else if (AuthorizationUtil.isPatient(authenticatedUser)) {
            if (status != null && !status.isEmpty()) {
                turns = turnService.getTurnsByPatientAndStatus(authenticatedUser.getId(), status);
            } else {
                turns = turnService.getTurnsByPatient(authenticatedUser.getId());
            }
        } else {
            return AuthorizationUtil.createInvalidRoleResponse();
        }
        
        return ResponseEntity.ok(turns);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Object> getTurnsByDoctor(
            @PathVariable UUID doctorId,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        ResponseEntity<Object> validationError = TurnAuthorizationUtil.validateDoctorTurnAccess(authenticatedUser, doctorId);
        if (validationError != null) {
            return validationError;
        }
        
        List<TurnResponseDTO> turns;
        if (status != null && !status.isEmpty()) {
            turns = turnService.getTurnsByDoctorAndStatus(doctorId, status);
        } else {
            turns = turnService.getTurnsByDoctor(doctorId);
        }
        
        return ResponseEntity.ok(turns);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Object> getTurnsByPatient(
            @PathVariable UUID patientId,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        ResponseEntity<Object> validationError = TurnAuthorizationUtil.validatePatientTurnAccess(authenticatedUser, patientId);
        if (validationError != null) {
            return validationError;
        }
        
        List<TurnResponseDTO> turns;
        if (status != null && !status.isEmpty()) {
            turns = turnService.getTurnsByPatientAndStatus(patientId, status);
        } else {
            turns = turnService.getTurnsByPatient(patientId);
        }
        
        return ResponseEntity.ok(turns);
    }
    
    @PatchMapping("/{turnId}/cancel")
    public ResponseEntity<Object> cancelTurn(
            @PathVariable UUID turnId,
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        if (!"PATIENT".equals(authenticatedUser.getRole()) && !"DOCTOR".equals(authenticatedUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only patients and doctors can cancel turns");
        }
        
        try {
            TurnResponseDTO canceledTurn = turnService.cancelTurn(turnId, authenticatedUser.getId(), authenticatedUser.getRole());
            return ResponseEntity.ok(canceledTurn);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        }
    }
}
