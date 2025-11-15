package com.medibook.api.controller;

import com.medibook.api.dto.CreateMedicalHistoryRequestDTO;
import com.medibook.api.dto.MedicalHistoryDTO;
import com.medibook.api.dto.UpdateMedicalHistoryContentRequestDTO;
import com.medibook.api.dto.UpdateMedicalHistoryRequestDTO;
import com.medibook.api.service.MedicalHistoryService;
import jakarta.validation.Valid;
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
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/doctor/{doctorId}/patients")
    @PreAuthorize("hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId)")
    public ResponseEntity<MedicalHistoryDTO> updatePatientMedicalHistory(
            @PathVariable UUID doctorId,
            @Valid @RequestBody UpdateMedicalHistoryRequestDTO requestDTO) {
        // This endpoint updates/creates medical history for a turn
        MedicalHistoryDTO medicalHistory = medicalHistoryService.addMedicalHistory(
                doctorId, 
                requestDTO.getTurnId(), 
                requestDTO.getMedicalHistory()
        );
        return ResponseEntity.ok(medicalHistory);
    }

    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId)")
    public ResponseEntity<MedicalHistoryDTO> addMedicalHistory(
            @PathVariable UUID doctorId,
            @Valid @RequestBody CreateMedicalHistoryRequestDTO requestDTO) {
        MedicalHistoryDTO medicalHistory = medicalHistoryService.addMedicalHistory(
                doctorId, 
                requestDTO.getTurnId(), 
                requestDTO.getContent()
        );
        return ResponseEntity.ok(medicalHistory);
    }

    @PutMapping("/doctor/{doctorId}/{historyId}")
    @PreAuthorize("hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId)")
    public ResponseEntity<MedicalHistoryDTO> updateMedicalHistory(
            @PathVariable UUID doctorId,
            @PathVariable UUID historyId,
            @Valid @RequestBody UpdateMedicalHistoryContentRequestDTO requestDTO) {
        MedicalHistoryDTO updatedHistory = medicalHistoryService.updateMedicalHistory(
                doctorId, 
                historyId, 
                requestDTO.getContent()
        );
        return ResponseEntity.ok(updatedHistory);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId)")
    public ResponseEntity<List<MedicalHistoryDTO>> getDoctorMedicalHistoryEntries(@PathVariable UUID doctorId) {
        List<MedicalHistoryDTO> histories = medicalHistoryService.getDoctorMedicalHistoryEntries(doctorId);
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/doctor/{doctorId}/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId) or hasRole('PATIENT') and authentication.principal.id.equals(#patientId) or hasRole('ADMIN')")
    public ResponseEntity<List<MedicalHistoryDTO>> getPatientMedicalHistoryByDoctor(
            @PathVariable UUID doctorId,
            @PathVariable UUID patientId) {
        List<MedicalHistoryDTO> histories = medicalHistoryService.getPatientMedicalHistoryByDoctor(patientId, doctorId);
        return ResponseEntity.ok(histories);
    }

    @DeleteMapping("/doctor/{doctorId}/{historyId}")
    @PreAuthorize("hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId)")
    public ResponseEntity<Void> deleteMedicalHistory(
            @PathVariable UUID doctorId,
            @PathVariable UUID historyId) {
        medicalHistoryService.deleteMedicalHistory(doctorId, historyId);
        return ResponseEntity.noContent().build();
    }
}