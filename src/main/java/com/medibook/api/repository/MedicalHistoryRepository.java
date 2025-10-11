package com.medibook.api.repository;

import com.medibook.api.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, UUID> {
    
    /**
     * Find all medical history entries for a specific patient ordered by creation date (newest first)
     */
    List<MedicalHistory> findByPatient_IdOrderByCreatedAtDesc(UUID patientId);
    
    /**
     * Find all medical history entries created by a specific doctor
     */
    List<MedicalHistory> findByDoctor_IdOrderByCreatedAtDesc(UUID doctorId);
    
    /**
     * Find all medical history entries for a specific patient created by a specific doctor
     */
    List<MedicalHistory> findByPatient_IdAndDoctor_IdOrderByCreatedAtDesc(UUID patientId, UUID doctorId);
    
    /**
     * Check if a doctor has any medical history entries for a patient
     */
    boolean existsByPatient_IdAndDoctor_Id(UUID patientId, UUID doctorId);
    
    /**
     * Check if a medical history already exists for the given turn
     */
    boolean existsByTurn_Id(UUID turnId);

    /**
     * Retrieve the medical history associated with a specific turn
     */
    Optional<MedicalHistory> findByTurn_Id(UUID turnId);

    /**
     * Get the latest medical history entry for a patient
     */
    MedicalHistory findFirstByPatient_IdOrderByCreatedAtDesc(UUID patientId);
}