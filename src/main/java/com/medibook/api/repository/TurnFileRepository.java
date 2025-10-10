package com.medibook.api.repository;

import com.medibook.api.entity.TurnFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TurnFileRepository extends JpaRepository<TurnFile, UUID> {
    
    Optional<TurnFile> findByTurnId(UUID turnId);
    
    boolean existsByTurnId(UUID turnId);
    
    @Modifying
    @Transactional
    void deleteByTurnId(UUID turnId);
}
