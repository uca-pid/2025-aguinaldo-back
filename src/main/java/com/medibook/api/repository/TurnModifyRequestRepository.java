package com.medibook.api.repository;

import com.medibook.api.entity.TurnModifyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TurnModifyRequestRepository extends JpaRepository<TurnModifyRequest, UUID> {
    
    List<TurnModifyRequest> findByPatient_IdOrderByIdDesc(UUID patientId);
    
    List<TurnModifyRequest> findByDoctor_IdAndStatusOrderByIdDesc(UUID doctorId, String status);
    
    Optional<TurnModifyRequest> findByTurnAssigned_IdAndStatus(UUID turnAssignedId, String status);
    
    @Query("SELECT tmr FROM TurnModifyRequest tmr " +
           "WHERE tmr.turnAssigned.id = :turnId " +
           "AND tmr.patient.id = :patientId " +
           "AND tmr.status = 'PENDING'")
    Optional<TurnModifyRequest> findPendingRequestByTurnAndPatient(
            @Param("turnId") UUID turnId, 
            @Param("patientId") UUID patientId);

       @org.springframework.data.jpa.repository.Modifying
       @org.springframework.data.jpa.repository.Query("DELETE FROM TurnModifyRequest t WHERE t.turnAssigned.id = :turnId AND t.status = :status")
       void deleteByTurnAssigned_IdAndStatus(@Param("turnId") UUID turnId, @Param("status") String status);
}