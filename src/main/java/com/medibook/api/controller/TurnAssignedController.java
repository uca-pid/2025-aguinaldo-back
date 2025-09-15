package com.medibook.api.controller;

import com.medibook.api.dto.Turn.TurnCreateRequestDTO;
import com.medibook.api.dto.Turn.TurnReserveRequestDTO;
import com.medibook.api.dto.Turn.TurnResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.service.TurnAssignedService;
import com.medibook.api.service.TurnAvailableService;
import com.medibook.api.util.AuthorizationUtil;
import com.medibook.api.util.TurnAuthorizationUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/turns")
@RequiredArgsConstructor
public class TurnAssignedController {

    private final TurnAssignedService turnService;
    private final TurnAvailableService availableService;

    @PostMapping
    public ResponseEntity<Object> createTurn(
            @RequestBody TurnCreateRequestDTO dto, 
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        if (AuthorizationUtil.isPatient(authenticatedUser)) {
            ResponseEntity<Object> validationError = TurnAuthorizationUtil.validatePatientTurnCreation(authenticatedUser, dto.getPatientId());
            if (validationError != null) {
                return validationError;
            }
        } else if (AuthorizationUtil.isDoctor(authenticatedUser)) {
            ResponseEntity<Object> validationError = TurnAuthorizationUtil.validateDoctorTurnCreation(authenticatedUser, dto.getDoctorId());
            if (validationError != null) {
                return validationError;
            }
        } else {
            return AuthorizationUtil.createInvalidRoleResponse();
        }
        
        TurnResponseDTO result = turnService.createTurn(dto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/available")
    public ResponseEntity<List<OffsetDateTime>> getAvailableTurns(
            @RequestParam UUID doctorId,
            @RequestParam String date,
            HttpServletRequest request) {
        
        LocalDate localDate = LocalDate.parse(date);
        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd = LocalTime.of(18, 0);
        
        List<OffsetDateTime> available = availableService.getAvailableTurns(
                doctorId, localDate, workStart, workEnd);
        
        return ResponseEntity.ok(available);
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
}
