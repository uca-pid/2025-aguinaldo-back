package com.medibook.api.controller;
import com.medibook.api.dto.Badge.BadgeProgressSummaryDTO;
import com.medibook.api.dto.Badge.DoctorBadgesResponseDTO;
import com.medibook.api.service.BadgeProgressService;
import com.medibook.api.service.DoctorBadgeService;
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

    private final DoctorBadgeService badgeService;
    private final BadgeProgressService badgeProgressService;

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DoctorBadgesResponseDTO> getDoctorBadges(@PathVariable UUID doctorId) {
        DoctorBadgesResponseDTO badges = badgeService.getDoctorBadges(doctorId);
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/my-badges")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorBadgesResponseDTO> getMyBadges(Authentication authentication) {
        UUID doctorId = ((com.medibook.api.entity.User) authentication.getPrincipal()).getId();
        DoctorBadgesResponseDTO badges = badgeService.getDoctorBadges(doctorId);
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/doctor/{doctorId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BadgeProgressSummaryDTO>> getDoctorBadgeProgress(@PathVariable UUID doctorId) {
        List<BadgeProgressSummaryDTO> progress = badgeProgressService.getBadgeProgress(doctorId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/my-progress")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<BadgeProgressSummaryDTO>> getMyBadgeProgress(Authentication authentication) {
        UUID doctorId = ((com.medibook.api.entity.User) authentication.getPrincipal()).getId();
        List<BadgeProgressSummaryDTO> progress = badgeProgressService.getBadgeProgress(doctorId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/doctor/{doctorId}/evaluate")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId))")
    public ResponseEntity<Void> evaluateDoctorBadges(@PathVariable UUID doctorId) {
        badgeService.evaluateAllBadges(doctorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/evaluate")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> evaluateMyBadges(Authentication authentication) {
        UUID doctorId = ((com.medibook.api.entity.User) authentication.getPrincipal()).getId();
        badgeService.evaluateAllBadges(doctorId);
        return ResponseEntity.ok().build();
    }
}