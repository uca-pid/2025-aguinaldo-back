package com.medibook.api.controller;

import com.medibook.api.dto.MedicalHistoryDTO;
import com.medibook.api.service.MedicalHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-history")
@RequiredArgsConstructor
@Slf4j
public class MedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT') and authentication.principal.id.equals(#patientId) or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<MedicalHistoryDTO>> getPatientMedicalHistory(@PathVariable UUID patientId) {
        List<MedicalHistoryDTO> histories = medicalHistoryService.getPatientMedicalHistory(patientId);
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/{historyId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<MedicalHistoryDTO> getMedicalHistoryById(@PathVariable UUID historyId) {
        // This would require adding a method to the service
        // For now, we'll return a method not implemented response
        return ResponseEntity.notFound().build();
    }
}