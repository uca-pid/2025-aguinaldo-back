package com.medibook.api.controller;
import com.medibook.api.dto.Badge.BadgeProgressSummaryDTO;
import com.medibook.api.dto.Badge.DoctorBadgesResponseDTO;
import com.medibook.api.dto.Badge.PatientBadgeProgressSummaryDTO;
import com.medibook.api.dto.Badge.PatientBadgesResponseDTO;
import com.medibook.api.service.BadgeProgressService;
import com.medibook.api.service.DoctorBadgeService;
import com.medibook.api.service.PatientBadgeProgressService;
import com.medibook.api.service.PatientBadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final DoctorBadgeService doctorBadgeService;
    private final BadgeProgressService doctorBadgeProgressService;
    private final PatientBadgeService patientBadgeService;
    private final PatientBadgeProgressService patientBadgeProgressService;

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DoctorBadgesResponseDTO> getDoctorBadges(@PathVariable UUID doctorId) {
        DoctorBadgesResponseDTO badges = doctorBadgeService.getDoctorBadges(doctorId);
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/my-badges")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorBadgesResponseDTO> getMyDoctorBadges(Authentication authentication) {
        UUID doctorId = ((com.medibook.api.entity.User) authentication.getPrincipal()).getId();
        DoctorBadgesResponseDTO badges = doctorBadgeService.getDoctorBadges(doctorId);
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/doctor/{doctorId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BadgeProgressSummaryDTO>> getDoctorBadgeProgress(@PathVariable UUID doctorId) {
        List<BadgeProgressSummaryDTO> progress = doctorBadgeProgressService.getBadgeProgress(doctorId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/doctor/my-progress")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<BadgeProgressSummaryDTO>> getMyDoctorBadgeProgress(Authentication authentication) {
        UUID doctorId = ((com.medibook.api.entity.User) authentication.getPrincipal()).getId();
        List<BadgeProgressSummaryDTO> progress = doctorBadgeProgressService.getBadgeProgress(doctorId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/doctor/{doctorId}/evaluate")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and #doctorId.equals(authentication.principal.id))")
    public ResponseEntity<Void> evaluateDoctorBadges(@PathVariable UUID doctorId) {
        doctorBadgeService.evaluateAllBadges(doctorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/doctor/evaluate")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> evaluateMyDoctorBadges(Authentication authentication) {
        UUID doctorId = ((com.medibook.api.entity.User) authentication.getPrincipal()).getId();
        doctorBadgeService.evaluateAllBadges(doctorId);
        return ResponseEntity.ok().build();
    }

    // ========== PATIENT BADGES ENDPOINTS ==========

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PatientBadgesResponseDTO> getPatientBadges(@PathVariable UUID patientId) {
        PatientBadgesResponseDTO badges = patientBadgeService.getPatientBadges(patientId);
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/patient/my-badges")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientBadgesResponseDTO> getMyPatientBadges(Authentication authentication) {
        UUID patientId = ((com.medibook.api.entity.User) authentication.getPrincipal()).getId();
        PatientBadgesResponseDTO badges = patientBadgeService.getPatientBadges(patientId);
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/patient/{patientId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PatientBadgeProgressSummaryDTO>> getPatientBadgeProgress(@PathVariable UUID patientId) {
        List<PatientBadgeProgressSummaryDTO> progress = patientBadgeProgressService.getBadgeProgress(patientId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/patient/my-progress")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PatientBadgeProgressSummaryDTO>> getMyPatientBadgeProgress(Authentication authentication) {
        UUID patientId = ((com.medibook.api.entity.User) authentication.getPrincipal()).getId();
        List<PatientBadgeProgressSummaryDTO> progress = patientBadgeProgressService.getBadgeProgress(patientId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/patient/{patientId}/evaluate")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PATIENT') and #patientId.equals(authentication.principal.id))")
    public ResponseEntity<Void> evaluatePatientBadges(@PathVariable UUID patientId) {
        patientBadgeService.evaluateAllBadges(patientId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/patient/evaluate")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> evaluateMyPatientBadges(Authentication authentication) {
        UUID patientId = ((com.medibook.api.entity.User) authentication.getPrincipal()).getId();
        patientBadgeService.evaluateAllBadges(patientId);
        return ResponseEntity.ok().build();
    }
}