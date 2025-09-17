package com.medibook.api.controller;

import com.medibook.api.dto.DoctorDTO;
import com.medibook.api.dto.PatientDTO;
import com.medibook.api.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        List<DoctorDTO> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<DoctorDTO>> getDoctorsBySpecialty(@PathVariable String specialty) {
        List<DoctorDTO> doctors = doctorService.getDoctorsBySpecialty(specialty);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/specialties")
    public ResponseEntity<List<String>> getAllSpecialties() {
        List<String> specialties = doctorService.getAllSpecialties();
        return ResponseEntity.ok(specialties);
    }

    @GetMapping("/{doctorId}/patients")
    @PreAuthorize("hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId)")
    public ResponseEntity<List<PatientDTO>> getPatientsByDoctor(@PathVariable UUID doctorId) {
        List<PatientDTO> patients = doctorService.getPatientsByDoctor(doctorId);
        return ResponseEntity.ok(patients);
    }
}