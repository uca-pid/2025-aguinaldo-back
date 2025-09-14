package com.medibook.api.repository;

import com.medibook.api.entity.TurnAssigned;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TurnAssignedRepository extends JpaRepository<TurnAssigned, UUID> {
    List<TurnAssigned> findByDoctor_IdAndScheduledAtBetween(UUID doctorId, OffsetDateTime start, OffsetDateTime end);
    boolean existsByDoctor_IdAndScheduledAt(UUID doctorId, OffsetDateTime scheduledAt);
}
