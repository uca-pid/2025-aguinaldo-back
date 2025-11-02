package com.medibook.api.repository;

import com.medibook.api.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    boolean existsByTurnAssigned_IdAndRater_Id(UUID turnId, UUID raterId);
    Optional<Rating> findByTurnAssigned_IdAndRater_Id(UUID turnId, UUID raterId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(r.score) FROM Rating r WHERE r.rated.id = :ratedId")
    Double findAverageScoreByRatedId(java.util.UUID ratedId);
    
    @Query("SELECT r FROM Rating r ORDER BY r.createdAt DESC")
    List<Rating> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT r FROM Rating r WHERE r.rater.role = :raterRole ORDER BY r.createdAt DESC")
    List<Rating> findAllByRaterRoleOrderByCreatedAtDesc(String raterRole);
    
    @Query("SELECT r FROM Rating r WHERE r.rated.role = :ratedRole ORDER BY r.createdAt DESC")
    List<Rating> findAllByRatedRoleOrderByCreatedAtDesc(String ratedRole);
}
