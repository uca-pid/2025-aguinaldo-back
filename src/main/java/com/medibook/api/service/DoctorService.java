package com.medibook.api.service;

import com.medibook.api.dto.DoctorDTO;
import com.medibook.api.dto.DoctorMetricsDTO;
import com.medibook.api.dto.MedicalHistoryDTO;
import com.medibook.api.dto.PatientDTO;
import com.medibook.api.dto.Rating.SubcategoryCountDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.DoctorMapper;
import com.medibook.api.repository.RatingRepository;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
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
    private final RatingRepository ratingRepository;

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

    public DoctorMetricsDTO getDoctorMetrics(UUID doctorId) {
        // Get doctor
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

        // Get all turns for the doctor
        List<TurnAssigned> allTurns = turnAssignedRepository.findByDoctor_IdOrderByScheduledAtDesc(doctorId);
        
        // Calculate metrics
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        
        // Upcoming turns (SCHEDULED status and in the future)
        int upcomingTurns = (int) allTurns.stream()
                .filter(turn -> "SCHEDULED".equals(turn.getStatus()) && turn.getScheduledAt().isAfter(now))
                .count();
        
        // Completed turns this month (COMPLETED status in current month)
        int completedTurnsThisMonth = (int) allTurns.stream()
                .filter(turn -> "COMPLETED".equals(turn.getStatus()) 
                        && turn.getScheduledAt().isAfter(startOfMonth) 
                        && turn.getScheduledAt().isBefore(now))
                .count();
        
        // Cancelled turns (all time)
        int cancelledTurns = (int) allTurns.stream()
                .filter(turn -> "CANCELED".equals(turn.getStatus()))
                .count();
        
        // Total patients
        List<User> patients = turnAssignedRepository.findDistinctPatientsByDoctorId(doctorId);
        int totalPatients = patients.size();
        
        // Get rating subcategories (ratings from patients about this doctor)
        List<RatingRepository.SubcategoryCount> subcategoryCounts = 
                ratingRepository.countSubcategoriesByRatedId(doctorId, "PATIENT");
        
        List<SubcategoryCountDTO> ratingSubcategories = subcategoryCounts.stream()
                .map(sc -> new SubcategoryCountDTO(sc.getSubcategory(), sc.getCount()))
                .collect(Collectors.toList());
        
        return DoctorMetricsDTO.builder()
                .doctorId(doctor.getId())
                .name(doctor.getName())
                .surname(doctor.getSurname())
                .specialty(doctor.getDoctorProfile() != null ? doctor.getDoctorProfile().getSpecialty() : null)
                .score(doctor.getScore())
                .ratingSubcategories(ratingSubcategories)
                .totalPatients(totalPatients)
                .upcomingTurns(upcomingTurns)
                .completedTurnsThisMonth(completedTurnsThisMonth)
                .cancelledTurns(cancelledTurns)
                .build();
    }
}