package com.medibook.api.repository;

import com.medibook.api.entity.BadgeStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BadgeStatisticsRepository extends JpaRepository<BadgeStatistics, UUID> {

    Optional<BadgeStatistics> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE BadgeStatistics bs SET bs.statistics = :statistics, bs.lastUpdatedAt = CURRENT_TIMESTAMP WHERE bs.userId = :userId")
    void updateStatistics(@Param("userId") UUID userId, @Param("statistics") String statistics);

    @Modifying
    @Query("UPDATE BadgeStatistics bs SET bs.progress = :progress, bs.lastUpdatedAt = CURRENT_TIMESTAMP WHERE bs.userId = :userId")
    void updateProgress(@Param("userId") UUID userId, @Param("progress") String progress);

    @Modifying
    @Query("UPDATE BadgeStatistics bs SET bs.statistics = :statistics, bs.progress = :progress, bs.lastUpdatedAt = CURRENT_TIMESTAMP WHERE bs.userId = :userId")
    void updateStatisticsAndProgress(@Param("userId") UUID userId, @Param("statistics") String statistics, @Param("progress") String progress);
}