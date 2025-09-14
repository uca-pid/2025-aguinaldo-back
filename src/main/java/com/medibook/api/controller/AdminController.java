package com.medibook.api.controller;

import com.medibook.api.dto.ErrorResponseDTO;
import com.medibook.api.entity.User;
import com.medibook.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/pending-doctors")
    public ResponseEntity<?> getPendingDoctors(HttpServletRequest request) {
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        if (!"ADMIN".equals(authenticatedUser.getRole())) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "ACCESS_DENIED", 
                "Only administrators can access this endpoint", 
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        try {
            List<User> pendingDoctors = userRepository.findByRoleAndStatus("DOCTOR", "PENDING");
            return ResponseEntity.ok(pendingDoctors);
        } catch (Exception e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "DATABASE_ERROR", 
                "Failed to retrieve pending doctors", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/approve-doctor/{doctorId}")
    public ResponseEntity<?> approveDoctor(
            @PathVariable UUID doctorId, 
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        if (!"ADMIN".equals(authenticatedUser.getRole())) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "ACCESS_DENIED", 
                "Only administrators can approve doctors", 
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        try {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

            if (!"DOCTOR".equals(doctor.getRole())) {
                ErrorResponseDTO error = ErrorResponseDTO.of(
                    "INVALID_USER_TYPE", 
                    "User is not a doctor", 
                    HttpStatus.BAD_REQUEST.value(),
                    request.getRequestURI()
                );
                return ResponseEntity.badRequest().body(error);
            }

            if (!"PENDING".equals(doctor.getStatus())) {
                ErrorResponseDTO error = ErrorResponseDTO.of(
                    "INVALID_STATUS", 
                    "Doctor is not in pending status. Current status: " + doctor.getStatus(), 
                    HttpStatus.BAD_REQUEST.value(),
                    request.getRequestURI()
                );
                return ResponseEntity.badRequest().body(error);
            }

            doctor.setStatus("ACTIVE");
            userRepository.save(doctor);

            return ResponseEntity.ok(Map.of(
                "message", "Doctor approved successfully",
                "doctorId", doctorId,
                "newStatus", "ACTIVE"
            ));
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "DOCTOR_NOT_FOUND", 
                e.getMessage(), 
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "APPROVAL_FAILED", 
                "Failed to approve doctor", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/reject-doctor/{doctorId}")
    public ResponseEntity<?> rejectDoctor(
            @PathVariable UUID doctorId, 
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        if (!"ADMIN".equals(authenticatedUser.getRole())) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "ACCESS_DENIED", 
                "Only administrators can reject doctors", 
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        try {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

            if (!"DOCTOR".equals(doctor.getRole())) {
                ErrorResponseDTO error = ErrorResponseDTO.of(
                    "INVALID_USER_TYPE", 
                    "User is not a doctor", 
                    HttpStatus.BAD_REQUEST.value(),
                    request.getRequestURI()
                );
                return ResponseEntity.badRequest().body(error);
            }

            if (!"PENDING".equals(doctor.getStatus())) {
                ErrorResponseDTO error = ErrorResponseDTO.of(
                    "INVALID_STATUS", 
                    "Doctor is not in pending status. Current status: " + doctor.getStatus(), 
                    HttpStatus.BAD_REQUEST.value(),
                    request.getRequestURI()
                );
                return ResponseEntity.badRequest().body(error);
            }

            doctor.setStatus("REJECTED");
            userRepository.save(doctor);

            return ResponseEntity.ok(Map.of(
                "message", "Doctor rejected successfully",
                "doctorId", doctorId,
                "newStatus", "REJECTED"
            ));
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "DOCTOR_NOT_FOUND", 
                e.getMessage(), 
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "REJECTION_FAILED", 
                "Failed to reject doctor", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        ErrorResponseDTO error = ErrorResponseDTO.of(
            "BAD_REQUEST", 
            ex.getMessage(), 
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        ErrorResponseDTO error = ErrorResponseDTO.of(
            "INTERNAL_SERVER_ERROR", 
            "An unexpected error occurred", 
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}