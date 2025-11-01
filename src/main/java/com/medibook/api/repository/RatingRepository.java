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
}
