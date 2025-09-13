package com.medibook.api.repository;

import com.medibook.api.entity.RefreshToken;
import com.medibook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") ZonedDateTime now);

    List<RefreshToken> findByUser(User user);

    List<RefreshToken> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.tokenHash = :tokenHash")
    int revokeTokenByHash(@Param("tokenHash") String tokenHash, @Param("revokedAt") ZonedDateTime revokedAt);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.user = :user AND rt.revokedAt IS NULL")
    int revokeAllTokensForUser(@Param("user") User user, @Param("revokedAt") ZonedDateTime revokedAt);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") ZonedDateTime now);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    long countValidTokensForUser(@Param("user") User user, @Param("now") ZonedDateTime now);
}