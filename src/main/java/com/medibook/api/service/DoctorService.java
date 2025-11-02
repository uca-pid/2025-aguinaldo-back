package com.medibook.api.service;

import com.medibook.api.dto.DoctorDTO;
import com.medibook.api.dto.MedicalHistoryDTO;
import com.medibook.api.dto.PatientDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.DoctorMapper;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final UserRepository userRepository;
    private final TurnAssignedRepository turnAssignedRepository;
    private final DoctorMapper doctorMapper;
    private final MedicalHistoryService medicalHistoryService;

    public List<DoctorDTO> getAllDoctors() {
        List<User> doctors = userRepository.findDoctorsByStatus("ACTIVE");
        return doctors.stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> getDoctorsBySpecialty(String specialty) {
        List<User> doctors = userRepository.findDoctorsByStatusAndSpecialty("ACTIVE", specialty);
        return doctors.stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<String> getAllSpecialties() {
        List<User> doctors = userRepository.findDoctorsByStatus("ACTIVE");
        return doctors.stream()
                .map(user -> user.getDoctorProfile().getSpecialty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<PatientDTO> getPatientsByDoctor(UUID doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (!"DOCTOR".equals(doctor.getRole()) || !"ACTIVE".equals(doctor.getStatus())) {
            throw new RuntimeException("Invalid doctor or doctor is not active");
        }

        List<User> patients = turnAssignedRepository.findDistinctPatientsByDoctorId(doctorId);
        
        return patients.stream()
                .map(this::mapPatientToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePatientMedicalHistory(UUID doctorId, UUID patientId, UUID turnId, String medicalHistory) {
        TurnAssigned turn = turnAssignedRepository.findById(turnId)
                .orElseThrow(() -> new RuntimeException("Turn not found"));

        if (!turn.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Doctor can only update medical history for their own turns");
        }

        if (turn.getPatient() == null || !turn.getPatient().getId().equals(patientId)) {
            throw new RuntimeException("Turn does not belong to the specified patient");
        }

        medicalHistoryService.addMedicalHistory(doctorId, turnId, medicalHistory);
    }

    private PatientDTO mapPatientToDTO(User patient) {
        List<MedicalHistoryDTO> medicalHistories = medicalHistoryService.getPatientMedicalHistory(patient.getId());
        
        String latestMedicalHistory = medicalHistoryService.getLatestMedicalHistoryContent(patient.getId());
        
        return PatientDTO.builder()
                .id(patient.getId())
                .name(patient.getName())
                .surname(patient.getSurname())
                .email(patient.getEmail())
                .dni(patient.getDni())
                .phone(patient.getPhone())
                .birthdate(patient.getBirthdate())
                .gender(patient.getGender())
                .status(patient.getStatus())
                .medicalHistories(medicalHistories)
                .medicalHistory(latestMedicalHistory)
                .score(patient.getScore())
                .build();
    }
}