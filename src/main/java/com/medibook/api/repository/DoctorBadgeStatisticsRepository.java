package com.medibook.api.repository;
import com.medibook.api.entity.DoctorBadgeStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorBadgeStatisticsRepository extends JpaRepository<DoctorBadgeStatistics, UUID> {
    
    Optional<DoctorBadgeStatistics> findByDoctorId(UUID doctorId);
    
    @Modifying
    @Query("UPDATE DoctorBadgeStatistics s SET " +
           "s.totalRatingsReceived = s.totalRatingsReceived + 1, " +
           "s.last50CommunicationCount = CASE WHEN :hasCommunication = true THEN s.last50CommunicationCount + 1 ELSE s.last50CommunicationCount END, " +
           "s.last50EmpathyCount = CASE WHEN :hasEmpathy = true THEN s.last50EmpathyCount + 1 ELSE s.last50EmpathyCount END, " +
           "s.last50PunctualityCount = CASE WHEN :hasPunctuality = true THEN s.last50PunctualityCount + 1 ELSE s.last50PunctualityCount END, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.doctorId = :doctorId")
    void incrementRatingCounters(@Param("doctorId") UUID doctorId, 
                                  @Param("hasCommunication") boolean hasCommunication,
                                  @Param("hasEmpathy") boolean hasEmpathy,
                                  @Param("hasPunctuality") boolean hasPunctuality);
    
    @Modifying
    @Query("UPDATE DoctorBadgeStatistics s SET " +
           "s.totalTurnsCompleted = s.totalTurnsCompleted + 1, " +
           "s.turnsLast90Days = s.turnsLast90Days + 1, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.doctorId = :doctorId")
    void incrementTurnCompleted(@Param("doctorId") UUID doctorId);
    
    @Modifying
    @Query("UPDATE DoctorBadgeStatistics s SET " +
           "s.totalTurnsCancelled = s.totalTurnsCancelled + 1, " +
           "s.cancellationsLast90Days = s.cancellationsLast90Days + 1, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.doctorId = :doctorId")
    void incrementTurnCancelled(@Param("doctorId") UUID doctorId);

    @Modifying
    @Query("UPDATE DoctorBadgeStatistics s SET " +
           "s.totalTurnsCancelled = s.totalTurnsCancelled + 1, " +
           "s.cancellationsLast90Days = s.cancellationsLast90Days + 1, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.doctorId = :doctorId")
    void incrementTurnNoShow(@Param("doctorId") UUID doctorId);
    
    @Modifying
    @Query("UPDATE DoctorBadgeStatistics s SET " +
           "s.last50DocumentedCount = s.last50DocumentedCount + 1, " +
           "s.last30DocumentedCount = s.last30DocumentedCount + 1, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.doctorId = :doctorId")
    void incrementDocumentation(@Param("doctorId") UUID doctorId);
    
    @Modifying
    @Query("UPDATE DoctorBadgeStatistics s SET " +
           "s.last10RequestsHandled = CASE WHEN s.last10RequestsHandled < 10 THEN s.last10RequestsHandled + 1 ELSE 10 END, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.doctorId = :doctorId")
    void incrementRequestHandled(@Param("doctorId") UUID doctorId);
}
