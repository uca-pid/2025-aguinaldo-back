package com.medibook.api.service;

import com.medibook.api.dto.MedicalHistoryDTO;
import com.medibook.api.entity.MedicalHistory;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.repository.MedicalHistoryRepository;
import com.medibook.api.repository.TurnAssignedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MedicalHistoryService {
    
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final TurnAssignedRepository turnAssignedRepository;

    /**
     * Add a new medical history entry
     */
    @Transactional
    public MedicalHistoryDTO addMedicalHistory(UUID doctorId, UUID turnId, String content) {
        TurnAssigned turn = turnAssignedRepository.findById(turnId)
                .orElseThrow(() -> new RuntimeException("Turn not found"));

        if (!turn.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Doctor can only add medical history for their own turns");
        }

        if (turn.getPatient() == null) {
            throw new RuntimeException("Turn must have an assigned patient before recording medical history");
        }

        User doctor = turn.getDoctor();
        if (!"DOCTOR".equals(doctor.getRole()) || !"ACTIVE".equals(doctor.getStatus())) {
            throw new RuntimeException("Invalid doctor or doctor is not active");
        }

        User patient = turn.getPatient();
        if (!"PATIENT".equals(patient.getRole()) || !"ACTIVE".equals(patient.getStatus())) {
            throw new RuntimeException("Invalid patient or patient is not active");
        }

        if (medicalHistoryRepository.existsByTurn_Id(turnId)) {
            throw new RuntimeException("Medical history already exists for this turn");
        }

        MedicalHistory medicalHistory = MedicalHistory.builder()
                .patient(patient)
                .doctor(doctor)
                .turn(turn)
                .content(content)
                .build();

        MedicalHistory savedHistory = medicalHistoryRepository.save(medicalHistory);
        log.info("Added medical history entry for turn {} (patient {} by doctor {})", turnId, patient.getId(), doctorId);

        return mapToDTO(savedHistory);
    }

    /**
     * Update an existing medical history entry (only the doctor who created it can update)
     */
    @Transactional
    public MedicalHistoryDTO updateMedicalHistory(UUID doctorId, UUID historyId, String content) {
        MedicalHistory medicalHistory = medicalHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Medical history entry not found"));

        if (!medicalHistory.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Doctor can only update their own medical history entries");
        }

        medicalHistory.setContent(content);
        medicalHistory.setUpdatedAt(LocalDateTime.now());

        MedicalHistory updatedHistory = medicalHistoryRepository.save(medicalHistory);
        log.info("Updated medical history entry {} by doctor {}", historyId, doctorId);
        
        return mapToDTO(updatedHistory);
    }

    /**
     * Get all medical history entries for a patient
     */
    public List<MedicalHistoryDTO> getPatientMedicalHistory(UUID patientId) {
        return medicalHistoryRepository.findByPatient_IdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all medical history entries created by a doctor
     */
    public List<MedicalHistoryDTO> getDoctorMedicalHistoryEntries(UUID doctorId) {
        return medicalHistoryRepository.findByDoctor_IdOrderByCreatedAtDesc(doctorId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get medical history entries for a specific patient created by a specific doctor
     */
    public List<MedicalHistoryDTO> getPatientMedicalHistoryByDoctor(UUID patientId, UUID doctorId) {
        return medicalHistoryRepository.findByPatient_IdAndDoctor_IdOrderByCreatedAtDesc(patientId, doctorId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get the latest medical history entry for a patient (for backward compatibility)
     */
    public String getLatestMedicalHistoryContent(UUID patientId) {
        MedicalHistory latestHistory = medicalHistoryRepository.findFirstByPatient_IdOrderByCreatedAtDesc(patientId);
        return latestHistory != null ? latestHistory.getContent() : null;
    }

    /**
     * Delete a medical history entry (only the doctor who created it can delete)
     */
    @Transactional
    public void deleteMedicalHistory(UUID doctorId, UUID historyId) {
        MedicalHistory medicalHistory = medicalHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Medical history entry not found"));

        if (!medicalHistory.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Doctor can only delete their own medical history entries");
        }

        medicalHistoryRepository.delete(medicalHistory);
        log.info("Deleted medical history entry {} by doctor {}", historyId, doctorId);
    }

    private MedicalHistoryDTO mapToDTO(MedicalHistory medicalHistory) {
        return MedicalHistoryDTO.builder()
                .id(medicalHistory.getId())
                .content(medicalHistory.getContent())
                .createdAt(medicalHistory.getCreatedAt())
                .updatedAt(medicalHistory.getUpdatedAt())
                .patientId(medicalHistory.getPatient().getId())
                .patientName(medicalHistory.getPatient().getName())
                .patientSurname(medicalHistory.getPatient().getSurname())
                .doctorId(medicalHistory.getDoctor().getId())
                .doctorName(medicalHistory.getDoctor().getName())
                .doctorSurname(medicalHistory.getDoctor().getSurname())
        .turnId(medicalHistory.getTurn() != null ? medicalHistory.getTurn().getId() : null)
                .build();
    }
}