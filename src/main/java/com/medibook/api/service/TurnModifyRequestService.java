package com.medibook.api.service;

import com.medibook.api.dto.Turn.TurnModifyRequestDTO;
import com.medibook.api.dto.Turn.TurnModifyRequestResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.TurnModifyRequest;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.TurnModifyRequestMapper;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.TurnModifyRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TurnModifyRequestService {
    
    private final TurnModifyRequestRepository turnModifyRequestRepository;
    private final TurnAssignedRepository turnAssignedRepository;
    private final TurnModifyRequestMapper mapper;
    
    @Transactional
    public TurnModifyRequestResponseDTO createModifyRequest(TurnModifyRequestDTO dto, User patient) {
        Optional<TurnAssigned> turnOpt = turnAssignedRepository.findById(dto.getTurnId());
        if (turnOpt.isEmpty()) {
            throw new IllegalArgumentException("Turn not found");
        }
        
        TurnAssigned turn = turnOpt.get();
        
        if (!turn.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Turn does not belong to this patient");
        }
        
        if (turn.getScheduledAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Cannot modify past appointments");
        }
        
        if (dto.getNewScheduledAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule appointments in the past");
        }
        
        Optional<TurnModifyRequest> existingRequest = turnModifyRequestRepository
                .findPendingRequestByTurnAndPatient(dto.getTurnId(), patient.getId());
        if (existingRequest.isPresent()) {
            throw new IllegalArgumentException("There is already a pending modification request for this appointment");
        }
        
        TurnModifyRequest modifyRequest = TurnModifyRequest.builder()
                .turnAssigned(turn)
                .patient(patient)
                .doctor(turn.getDoctor())
                .currentScheduledAt(turn.getScheduledAt())
                .requestedScheduledAt(dto.getNewScheduledAt())
                .status("PENDING")
                .build();
        
        TurnModifyRequest savedRequest = turnModifyRequestRepository.save(modifyRequest);
                
        return mapper.toResponseDTO(savedRequest);
    }
    
    public List<TurnModifyRequestResponseDTO> getPatientRequests(UUID patientId) {
        List<TurnModifyRequest> requests = turnModifyRequestRepository.findByPatient_IdOrderByIdDesc(patientId);
        return requests.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<TurnModifyRequestResponseDTO> getDoctorPendingRequests(UUID doctorId) {
        List<TurnModifyRequest> requests = turnModifyRequestRepository
                .findByDoctor_IdAndStatusOrderByIdDesc(doctorId, "PENDING");
        return requests.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TurnModifyRequestResponseDTO approveModifyRequest(UUID requestId, User doctor) {
        Optional<TurnModifyRequest> requestOpt = turnModifyRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Modify request not found");
        }

        TurnModifyRequest request = requestOpt.get();

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("You can only approve requests for your own appointments");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("Request is not pending");
        }

        TurnAssigned turn = request.getTurnAssigned();
        turn.setScheduledAt(request.getRequestedScheduledAt());
        turnAssignedRepository.save(turn);

        request.setStatus("APPROVED");
        TurnModifyRequest savedRequest = turnModifyRequestRepository.save(request);

        return mapper.toResponseDTO(savedRequest);
    }

    @Transactional
    public TurnModifyRequestResponseDTO rejectModifyRequest(UUID requestId, User doctor) {
        Optional<TurnModifyRequest> requestOpt = turnModifyRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Modify request not found");
        }

        TurnModifyRequest request = requestOpt.get();

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("You can only reject requests for your own appointments");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("Request is not pending");
        }

        request.setStatus("REJECTED");
        TurnModifyRequest savedRequest = turnModifyRequestRepository.save(request);

        return mapper.toResponseDTO(savedRequest);
    }
}