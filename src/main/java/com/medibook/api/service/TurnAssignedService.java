package com.medibook.api.service;

import com.medibook.api.dto.TurnCreateRequestDTO;
import com.medibook.api.dto.TurnResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.TurnAssignedMapper;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TurnAssignedService {

    private final TurnAssignedRepository turnRepo;
    private final UserRepository userRepo;
    private final TurnAssignedMapper mapper;

    // Crear turno dinámicamente cuando paciente lo selecciona
    public TurnResponseDTO createTurn(TurnCreateRequestDTO dto) {
        // Validar que el doctor existe
        User doctor = userRepo.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        // Validar que el paciente existe
        User patient = userRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Verificar que el horario esté disponible
        boolean slotTaken = turnRepo.existsByDoctor_IdAndScheduledAt(
                dto.getDoctorId(), dto.getScheduledAt());
        
        if (slotTaken) {
            throw new RuntimeException("Time slot is already taken");
        }

        // Crear nuevo turno
        TurnAssigned turn = TurnAssigned.builder()
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(dto.getScheduledAt())
                .status("SCHEDULED")
                .build();

        TurnAssigned saved = turnRepo.save(turn);
        return mapper.toDTO(saved);
    }

    // Método existente para reservar turnos ya creados
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
}
