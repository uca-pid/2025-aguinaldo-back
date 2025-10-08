package com.medibook.api.controller;

import com.medibook.api.dto.ErrorResponseDTO;
import com.medibook.api.dto.Admin.AdminStatsDTO;
import com.medibook.api.dto.Admin.DoctorApprovalResponseDTO;
import com.medibook.api.dto.Admin.PendingDoctorDTO;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.AdminMapper;
import com.medibook.api.repository.UserRepository;
import com.medibook.api.service.email.EmailService;
import com.medibook.api.util.AuthorizationUtil;
import com.medibook.api.util.ErrorResponseUtil;
import com.medibook.api.util.UserValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final AdminMapper adminMapper;
    private final EmailService emailService;

    public AdminController(UserRepository userRepository, AdminMapper adminMapper, EmailService emailService) {
        this.userRepository = userRepository;
        this.adminMapper = adminMapper;
        this.emailService = emailService;
    }

    @GetMapping("/pending-doctors")
    public ResponseEntity<?> getPendingDoctors(HttpServletRequest request) {
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        if (!AuthorizationUtil.isAdmin(authenticatedUser)) {
            return AuthorizationUtil.createAdminAccessDeniedResponse(request.getRequestURI());
        }

        try {
            List<User> pendingDoctors = userRepository.findByRoleAndStatus("DOCTOR", "PENDING");
            List<PendingDoctorDTO> pendingDoctorDTOs = pendingDoctors.stream()
                .map(adminMapper::convertToPendingDoctorDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(pendingDoctorDTOs);
        } catch (Exception e) {
            return ErrorResponseUtil.createDatabaseErrorResponse(request.getRequestURI());
        }
    }

    @PostMapping("/approve-doctor/{doctorId}")
    public ResponseEntity<?> approveDoctor(
            @PathVariable UUID doctorId, 
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        if (!AuthorizationUtil.isAdmin(authenticatedUser)) {
            return AuthorizationUtil.createAdminAccessDeniedResponse(request.getRequestURI());
        }

        try {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

            ResponseEntity<ErrorResponseDTO> validationError = UserValidationUtil.validateDoctorForStatusChange(doctor, request.getRequestURI());
            if (validationError != null) {
                return validationError;
            }

            doctor.setStatus("ACTIVE");
            userRepository.save(doctor);

            try {
                emailService.sendApprovalEmailToDoctor(doctor.getEmail(), doctor.getName());
            } catch (Exception e) {
                log.warn("⚠️ No se pudo enviar email de aprobación al doctor: {}", e.getMessage());
            }

            DoctorApprovalResponseDTO response = new DoctorApprovalResponseDTO(
                "Doctor approved successfully",
                doctorId.toString(),
                "ACTIVE"
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ErrorResponseUtil.createDoctorNotFoundResponse(e.getMessage(), request.getRequestURI());
        } catch (Exception e) {
            return ErrorResponseUtil.createErrorResponse(
                "APPROVAL_FAILED", 
                "Failed to approve doctor", 
                HttpStatus.INTERNAL_SERVER_ERROR, 
                request.getRequestURI()
            );
        }
    }

    @PostMapping("/reject-doctor/{doctorId}")
    public ResponseEntity<?> rejectDoctor(
            @PathVariable UUID doctorId, 
            HttpServletRequest request) {
        
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        if (!AuthorizationUtil.isAdmin(authenticatedUser)) {
            return AuthorizationUtil.createAdminAccessDeniedResponse(request.getRequestURI());
        }

        try {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

            ResponseEntity<ErrorResponseDTO> validationError = UserValidationUtil.validateDoctorForStatusChange(doctor, request.getRequestURI());
            if (validationError != null) {
                return validationError;
            }

            doctor.setStatus("REJECTED");
            userRepository.save(doctor);

            try {
                emailService.sendRejectionEmailToDoctor(doctor.getEmail(), doctor.getName(), 
                    "Tu solicitud de registro como médico no ha sido aprobada. Por favor, contacta al equipo de soporte si necesitas más información.");
            } catch (Exception e) {
                log.warn("⚠️ No se pudo enviar email de rechazo al doctor: {}", e.getMessage());
            }

            DoctorApprovalResponseDTO response = new DoctorApprovalResponseDTO(
                "Doctor rejected successfully",
                doctorId.toString(),
                "REJECTED"
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ErrorResponseUtil.createDoctorNotFoundResponse(e.getMessage(), request.getRequestURI());
        } catch (Exception e) {
            return ErrorResponseUtil.createErrorResponse(
                "REJECTION_FAILED", 
                "Failed to reject doctor", 
                HttpStatus.INTERNAL_SERVER_ERROR, 
                request.getRequestURI()
            );
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats(HttpServletRequest request) {
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        
        if (!AuthorizationUtil.isAdmin(authenticatedUser)) {
            return AuthorizationUtil.createAdminAccessDeniedResponse(request.getRequestURI());
        }

        try {
            long patientsCount = userRepository.countByRole("PATIENT");
            long doctorsCount = userRepository.countByRoleAndStatus("DOCTOR", "ACTIVE");
            long pendingCount = userRepository.countByRoleAndStatus("DOCTOR", "PENDING");

            AdminStatsDTO stats = new AdminStatsDTO(
                (int) patientsCount,
                (int) doctorsCount,
                (int) pendingCount
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ErrorResponseUtil.createDatabaseErrorResponse(request.getRequestURI());
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        return ErrorResponseUtil.createBadRequestResponse(ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        return ErrorResponseUtil.createInternalServerErrorResponse(
            "An unexpected error occurred", 
            request.getRequestURI()
        );
    }
}