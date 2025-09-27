package com.medibook.api.service;

import com.medibook.api.dto.DoctorDTO;
import com.medibook.api.dto.PatientDTO;
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
    public void updatePatientMedicalHistory(UUID doctorId, UUID patientId, String medicalHistory) {
        // Verify doctor exists and is active
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (!"DOCTOR".equals(doctor.getRole()) || !"ACTIVE".equals(doctor.getStatus())) {
            throw new RuntimeException("Invalid doctor or doctor is not active");
        }

        // Verify patient exists and is a patient
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        if (!"PATIENT".equals(patient.getRole()) || !"ACTIVE".equals(patient.getStatus())) {
            throw new RuntimeException("Invalid patient or patient is not active");
        }

        // Verify the doctor has had appointments with this patient
        boolean hasAppointments = turnAssignedRepository.existsByDoctor_IdAndPatient_Id(doctorId, patientId);
        if (!hasAppointments) {
            throw new RuntimeException("Doctor can only update medical history for patients they have treated");
        }

        // Update medical history
        patient.setMedicalHistory(medicalHistory);
        userRepository.save(patient);
    }

    private PatientDTO mapPatientToDTO(User patient) {
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
                .medicalHistory(patient.getMedicalHistory())
                .build();
    }
}