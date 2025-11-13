package com.medibook.api.repository;
import com.medibook.api.entity.PatientBadge;
import com.medibook.api.entity.PatientBadgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientBadgeRepository extends JpaRepository<PatientBadge, UUID> {

    List<PatientBadge> findByPatient_IdAndIsActiveTrue(UUID patientId);

    List<PatientBadge> findByPatient_IdOrderByEarnedAtDesc(UUID patientId);

    Optional<PatientBadge> findByPatient_IdAndBadgeType(UUID patientId, PatientBadgeType badgeType);

    boolean existsByPatient_IdAndBadgeType(UUID patientId, PatientBadgeType badgeType);

    boolean existsByPatient_IdAndBadgeTypeAndIsActive(UUID patientId, PatientBadgeType badgeType, boolean isActive);

    @Query("SELECT COUNT(b) FROM PatientBadge b WHERE b.patient.id = :patientId AND b.isActive = true")
    long countActiveBadgesByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT COUNT(b) FROM PatientBadge b WHERE b.patient.id = :patientId AND b.isActive = true AND b.badgeType != :excludeType")
    long countActiveBadgesByPatientIdExcludingType(@Param("patientId") UUID patientId, @Param("excludeType") PatientBadgeType excludeType);

    List<PatientBadge> findByBadgeTypeAndIsActiveTrue(PatientBadgeType badgeType);
}