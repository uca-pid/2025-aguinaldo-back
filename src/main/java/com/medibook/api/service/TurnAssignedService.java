package com.medibook.api.service;

import com.medibook.api.dto.Turn.TurnCreateRequestDTO;
import com.medibook.api.dto.Turn.TurnResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.TurnAssignedMapper;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TurnAssignedService {

    private final TurnAssignedRepository turnRepo;
    private final UserRepository userRepo;
    private final TurnAssignedMapper mapper;

    public TurnResponseDTO createTurn(TurnCreateRequestDTO dto) {
        User doctor = userRepo.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }
        
        if (!"ACTIVE".equals(doctor.getStatus())) {
            throw new RuntimeException("Doctor is not active");
        }
        
        User patient = userRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!"PATIENT".equals(patient.getRole())) {
            throw new RuntimeException("User is not a patient");
        }
        
        if (!"ACTIVE".equals(patient.getStatus())) {
            throw new RuntimeException("Patient is not active");
        }

        boolean slotTaken = turnRepo.existsByDoctor_IdAndScheduledAt(
                dto.getDoctorId(), dto.getScheduledAt());
        
        if (slotTaken) {
            throw new RuntimeException("Time slot is already taken");
        }

        TurnAssigned turn = TurnAssigned.builder()
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(dto.getScheduledAt())
                .status("PENDING")
                .build();

        TurnAssigned saved = turnRepo.save(turn);
        return mapper.toDTO(saved);
    }

    public TurnAssigned reserveTurn(UUID turnId, UUID patientId) {
        TurnAssigned turn = turnRepo.findById(turnId)
                .orElseThrow(() -> new RuntimeException("Turn not found"));

        if (!"AVAILABLE".equals(turn.getStatus())) {
            throw new RuntimeException("Turn is not available");
        }

        User patient = userRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        turn.setPatient(patient);
        turn.setStatus("RESERVED");

        return turnRepo.save(turn);
    }
    
    public List<TurnResponseDTO> getTurnsByDoctor(UUID doctorId) {
        List<TurnAssigned> turns = turnRepo.findByDoctor_IdOrderByScheduledAtDesc(doctorId);
        return turns.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<TurnResponseDTO> getTurnsByPatient(UUID patientId) {
        List<TurnAssigned> turns = turnRepo.findByPatient_IdOrderByScheduledAtDesc(patientId);
        return turns.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<TurnResponseDTO> getTurnsByDoctorAndStatus(UUID doctorId, String status) {
        List<TurnAssigned> turns = turnRepo.findByDoctor_IdAndStatusOrderByScheduledAtDesc(doctorId, status);
        return turns.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<TurnResponseDTO> getTurnsByPatientAndStatus(UUID patientId, String status) {
        List<TurnAssigned> turns = turnRepo.findByPatient_IdAndStatusOrderByScheduledAtDesc(patientId, status);
        return turns.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public TurnResponseDTO cancelTurn(UUID turnId, UUID userId, String userRole) {
        TurnAssigned turn = turnRepo.findById(turnId)
                .orElseThrow(() -> new RuntimeException("Turn not found"));
        
        if ("PATIENT".equals(userRole)) {
            if (turn.getPatient() == null || !turn.getPatient().getId().equals(userId)) {
                throw new RuntimeException("You can only cancel your own turns");
            }
        } else if ("DOCTOR".equals(userRole)) {
            if (turn.getDoctor() == null || !turn.getDoctor().getId().equals(userId)) {
                throw new RuntimeException("You can only cancel your own turns");
            }
        } else {
            throw new RuntimeException("Invalid user role for cancellation");
        }
        
        if (!"SCHEDULED".equals(turn.getStatus()) && !"RESERVED".equals(turn.getStatus())) {
            throw new RuntimeException("Turn cannot be canceled. Current status: " + turn.getStatus());
        }
        
        OffsetDateTime now = OffsetDateTime.now();
        if (turn.getScheduledAt().isBefore(now)) {
            throw new RuntimeException("Cannot cancel past turns");
        }
        
        turn.setStatus("CANCELED");
        TurnAssigned saved = turnRepo.save(turn);
        
        return mapper.toDTO(saved);
    }
}
