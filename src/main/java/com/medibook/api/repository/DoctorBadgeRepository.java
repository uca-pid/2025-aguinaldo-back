package com.medibook.api.repository;
import com.medibook.api.entity.BadgeType;
import com.medibook.api.entity.DoctorBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorBadgeRepository extends JpaRepository<DoctorBadge, UUID> {
    
    List<DoctorBadge> findByDoctor_IdAndIsActiveTrue(UUID doctorId);
    
    List<DoctorBadge> findByDoctor_IdOrderByEarnedAtDesc(UUID doctorId);

    Optional<DoctorBadge> findByDoctor_IdAndBadgeType(UUID doctorId, BadgeType badgeType);
    
    boolean existsByDoctor_IdAndBadgeType(UUID doctorId, BadgeType badgeType);
    
    boolean existsByDoctor_IdAndBadgeTypeAndIsActive(UUID doctorId, BadgeType badgeType, boolean isActive);
    
    @Query("SELECT COUNT(b) FROM DoctorBadge b WHERE b.doctor.id = :doctorId AND b.isActive = true")
    long countActiveBadgesByDoctorId(@Param("doctorId") UUID doctorId);
    
    @Query("SELECT COUNT(b) FROM DoctorBadge b WHERE b.doctor.id = :doctorId AND b.isActive = true AND b.badgeType != :excludeType")
    long countActiveBadgesByDoctorIdExcludingType(@Param("doctorId") UUID doctorId, @Param("excludeType") BadgeType excludeType);
    
    List<DoctorBadge> findByBadgeTypeAndIsActiveTrue(BadgeType badgeType);
}
