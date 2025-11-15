package com.medibook.api.controller;

import com.medibook.api.dto.Availability.*;
import com.medibook.api.dto.DoctorDTO;
import com.medibook.api.dto.DoctorMetricsDTO;
import com.medibook.api.dto.PatientDTO;
import com.medibook.api.service.DoctorAvailabilityService;
import com.medibook.api.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Slf4j
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorAvailabilityService availabilityService;

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

    @PostMapping("/{doctorId}/availability")
    @PreAuthorize("hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId)")
    public ResponseEntity<Void> saveAvailability(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorAvailabilityRequestDTO request) {
        
        availabilityService.saveAvailability(doctorId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{doctorId}/availability")
    @PreAuthorize("hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId)")
    public ResponseEntity<DoctorAvailabilityResponseDTO> getAvailability(@PathVariable UUID doctorId) {
        DoctorAvailabilityResponseDTO availability = availabilityService.getAvailability(doctorId);
        return ResponseEntity.ok(availability);
    }

    @GetMapping("/{doctorId}/available-slots")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<AvailableSlotDTO> slots = availabilityService.getAvailableSlots(doctorId, fromDate, toDate);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/{doctorId}/available-slots-with-occupancy")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlotsWithOccupancy(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<AvailableSlotDTO> slots = availabilityService.getAvailableSlotsWithOccupancy(doctorId, fromDate, toDate);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/{doctorId}/metrics")
    @PreAuthorize("hasRole('DOCTOR') and authentication.principal.id.equals(#doctorId)")
    public ResponseEntity<DoctorMetricsDTO> getDoctorMetrics(@PathVariable UUID doctorId) {
        DoctorMetricsDTO metrics = doctorService.getDoctorMetrics(doctorId);
        return ResponseEntity.ok(metrics);
    }
}