package com.medibook.api.repository;

import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TurnAssignedRepository extends JpaRepository<TurnAssigned, UUID> {
    List<TurnAssigned> findByDoctor_IdAndScheduledAtBetween(UUID doctorId, OffsetDateTime start, OffsetDateTime end);
    boolean existsByDoctor_IdAndScheduledAt(UUID doctorId, OffsetDateTime scheduledAt);
    
    List<TurnAssigned> findByDoctor_IdOrderByScheduledAtDesc(UUID doctorId);
    
    List<TurnAssigned> findByPatient_IdOrderByScheduledAtDesc(UUID patientId);
    
    List<TurnAssigned> findByDoctor_IdAndStatusOrderByScheduledAtDesc(UUID doctorId, String status);
    
    List<TurnAssigned> findByPatient_IdAndStatusOrderByScheduledAtDesc(UUID patientId, String status);
    
    @Query("SELECT DISTINCT t.patient FROM TurnAssigned t WHERE t.doctor.id = :doctorId AND t.patient IS NOT NULL ORDER BY t.patient.name, t.patient.surname")
    List<User> findDistinctPatientsByDoctorId(@Param("doctorId") UUID doctorId);
}
