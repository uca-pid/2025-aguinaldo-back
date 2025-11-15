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

    @org.springframework.data.jpa.repository.Query("SELECT r.subcategory AS subcategory, COUNT(r) AS count FROM Rating r WHERE r.rated.id = :ratedId AND (:raterRole IS NULL OR r.rater.role = :raterRole) GROUP BY r.subcategory")
    java.util.List<SubcategoryCount> countSubcategoriesByRatedId(@org.springframework.data.repository.query.Param("ratedId") java.util.UUID ratedId, @org.springframework.data.repository.query.Param("raterRole") String raterRole);

    @Query("SELECT r FROM Rating r WHERE r.rated.id = :ratedId ORDER BY r.createdAt DESC")
    List<Rating> findTop100ByRatedIdOrderByCreatedAtDesc(@org.springframework.data.repository.query.Param("ratedId") UUID ratedId);

    @Query("SELECT r FROM Rating r WHERE r.rater.id = :raterId ORDER BY r.createdAt DESC")
    List<Rating> findByRaterId(@org.springframework.data.repository.query.Param("raterId") UUID raterId);

    @Query("SELECT r FROM Rating r WHERE r.rated.id = :ratedId ORDER BY r.createdAt DESC")
    List<Rating> findByRatedId(@org.springframework.data.repository.query.Param("ratedId") UUID ratedId);

    interface SubcategoryCount {
        String getSubcategory();
        Long getCount();
    }
}
