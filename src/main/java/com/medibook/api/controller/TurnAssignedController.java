package com.medibook.api.controller;

import com.medibook.api.dto.TurnCreateRequestDTO;
import com.medibook.api.dto.TurnReserveRequestDTO;
import com.medibook.api.dto.TurnResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.service.TurnAssignedService;
import com.medibook.api.service.TurnAvailableService;
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
        
        if ("PATIENT".equals(authenticatedUser.getRole())) {
            if (!authenticatedUser.getId().equals(dto.getPatientId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Patients can only create turns for themselves");
            }
        } else if ("DOCTOR".equals(authenticatedUser.getRole())) {
            if (!authenticatedUser.getId().equals(dto.getDoctorId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Doctors can only create turns for themselves");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Invalid user role");
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
        
        if (!"PATIENT".equals(authenticatedUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only patients can reserve turns");
        }
        
        if (!authenticatedUser.getId().equals(dto.getPatientId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Patients can only reserve turns for themselves");
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
        
        if ("DOCTOR".equals(authenticatedUser.getRole())) {
            if (status != null && !status.isEmpty()) {
                turns = turnService.getTurnsByDoctorAndStatus(authenticatedUser.getId(), status);
            } else {
                turns = turnService.getTurnsByDoctor(authenticatedUser.getId());
            }
        } else if ("PATIENT".equals(authenticatedUser.getRole())) {
            if (status != null && !status.isEmpty()) {
                turns = turnService.getTurnsByPatientAndStatus(authenticatedUser.getId(), status);
            } else {
                turns = turnService.getTurnsByPatient(authenticatedUser.getId());
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Invalid user role");
        }
        
        return ResponseEntity.ok(turns);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Object> getTurnsByDoctor(
            @PathVariable UUID doctorId,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        if (!"DOCTOR".equals(authenticatedUser.getRole()) || 
            !authenticatedUser.getId().equals(doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You can only view your own turns");
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
        
        if (!"PATIENT".equals(authenticatedUser.getRole()) || 
            !authenticatedUser.getId().equals(patientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You can only view your own turns");
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
