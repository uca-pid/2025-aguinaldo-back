package com.medibook.api.repository;

import com.medibook.api.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    boolean existsByTurnAssigned_IdAndRater_Id(UUID turnId, UUID raterId);
    Optional<Rating> findByTurnAssigned_IdAndRater_Id(UUID turnId, UUID raterId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(r.score) FROM Rating r WHERE r.rated.id = :ratedId")
    Double findAverageScoreByRatedId(java.util.UUID ratedId);

    @org.springframework.data.jpa.repository.Query("SELECT r.subcategory AS subcategory, COUNT(r) AS count FROM Rating r WHERE r.rated.id = :ratedId AND (:raterRole IS NULL OR r.rater.role = :raterRole) GROUP BY r.subcategory")
    java.util.List<SubcategoryCount> countSubcategoriesByRatedId(@org.springframework.data.repository.query.Param("ratedId") java.util.UUID ratedId, @org.springframework.data.repository.query.Param("raterRole") String raterRole);

    // Projection for repository result
    interface SubcategoryCount {
        String getSubcategory();
        Long getCount();
    }
}
