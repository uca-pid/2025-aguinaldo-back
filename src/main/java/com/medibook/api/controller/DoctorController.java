package com.medibook.api.controller;

import com.medibook.api.dto.Availability.*;
import com.medibook.api.dto.DoctorDTO;
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
        
        log.info("Received availability save request for doctorId: {}", doctorId);
        log.info("Request payload: {}", request);
        log.info("SlotDurationMin: {}", request.getSlotDurationMin());
        log.info("WeeklyAvailability size: {}", request.getWeeklyAvailability() != null ? request.getWeeklyAvailability().size() : "null");
        
        if (request.getWeeklyAvailability() != null) {
            for (int i = 0; i < request.getWeeklyAvailability().size(); i++) {
                DayAvailabilityDTO day = request.getWeeklyAvailability().get(i);
                log.info("Day {}: day={}, enabled={}, ranges size={}", 
                    i, day.getDay(), day.getEnabled(), 
                    day.getRanges() != null ? day.getRanges().size() : "null");
                
                if (day.getRanges() != null) {
                    for (int j = 0; j < day.getRanges().size(); j++) {
                        TimeRangeDTO range = day.getRanges().get(j);
                        log.info("  Range {}: start={}, end={}", j, range.getStart(), range.getEnd());
                    }
                }
            }
        }
        
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
}