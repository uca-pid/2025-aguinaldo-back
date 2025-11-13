package com.medibook.api.repository;
import com.medibook.api.entity.PatientBadgeStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientBadgeStatisticsRepository extends JpaRepository<PatientBadgeStatistics, UUID> {

    Optional<PatientBadgeStatistics> findByPatientId(UUID patientId);

    @Modifying
    @Query("UPDATE PatientBadgeStatistics s SET " +
           "s.totalTurnsCompleted = s.totalTurnsCompleted + 1, " +
           "s.turnsLast12Months = s.turnsLast12Months + 1, " +
           "s.turnsLast6Months = s.turnsLast6Months + 1, " +
           "s.turnsLast90Days = s.turnsLast90Days + 1, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.patientId = :patientId")
    void incrementTurnCompleted(@Param("patientId") UUID patientId);

    @Modifying
    @Query("UPDATE PatientBadgeStatistics s SET " +
           "s.totalTurnsCancelled = s.totalTurnsCancelled + 1, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.patientId = :patientId")
    void incrementTurnCancelled(@Param("patientId") UUID patientId);

    @Modifying
    @Query("UPDATE PatientBadgeStatistics s SET " +
           "s.totalTurnsNoShow = s.totalTurnsNoShow + 1, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.patientId = :patientId")
    void incrementTurnNoShow(@Param("patientId") UUID patientId);

    @Modifying
    @Query("UPDATE PatientBadgeStatistics s SET " +
           "s.totalRatingsGiven = s.totalRatingsGiven + 1, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.patientId = :patientId")
    void incrementRatingGiven(@Param("patientId") UUID patientId);

    @Modifying
    @Query("UPDATE PatientBadgeStatistics s SET " +
           "s.totalRatingsReceived = s.totalRatingsReceived + 1, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.patientId = :patientId")
    void incrementRatingReceived(@Param("patientId") UUID patientId);

    @Modifying
    @Query("UPDATE PatientBadgeStatistics s SET " +
           "s.last10TurnsFilesUploadedCount = CASE WHEN s.last10TurnsFilesUploadedCount < 10 THEN s.last10TurnsFilesUploadedCount + 1 ELSE 10 END, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.patientId = :patientId")
    void incrementFileUploaded(@Param("patientId") UUID patientId);

    @Modifying
    @Query("UPDATE PatientBadgeStatistics s SET " +
           "s.last5TurnsAdvanceBookingCount = CASE WHEN s.last5TurnsAdvanceBookingCount < 5 THEN s.last5TurnsAdvanceBookingCount + 1 ELSE 5 END, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.patientId = :patientId")
    void incrementAdvanceBooking(@Param("patientId") UUID patientId);

    @Modifying
    @Query("UPDATE PatientBadgeStatistics s SET " +
           "s.last10TurnsPunctualCount = CASE WHEN s.last10TurnsPunctualCount < 10 THEN s.last10TurnsPunctualCount + 1 ELSE 10 END, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.patientId = :patientId")
    void incrementPunctualRating(@Param("patientId") UUID patientId);

    @Modifying
    @Query("UPDATE PatientBadgeStatistics s SET " +
           "s.last15TurnsCollaborationCount = CASE WHEN s.last15TurnsCollaborationCount < 15 THEN s.last15TurnsCollaborationCount + 1 ELSE 15 END, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.patientId = :patientId")
    void incrementCollaborationRating(@Param("patientId") UUID patientId);

    @Modifying
    @Query("UPDATE PatientBadgeStatistics s SET " +
           "s.last15TurnsFollowInstructionsCount = CASE WHEN s.last15TurnsFollowInstructionsCount < 15 THEN s.last15TurnsFollowInstructionsCount + 1 ELSE 15 END, " +
           "s.lastUpdatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.patientId = :patientId")
    void incrementFollowInstructionsRating(@Param("patientId") UUID patientId);
}