package com.medibook.api.repository;

import com.medibook.api.entity.RefreshToken;
import com.medibook.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RefreshTokenRepositoryDeactivationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User user;
    private RefreshToken token1;
    private RefreshToken token2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setName("Test");
        user.setSurname("User");
        user.setStatus("ACTIVE");
        user.setRole("PATIENT");
        user.setDni(12345678L);
        user = entityManager.persistAndFlush(user);

        token1 = new RefreshToken();
        token1.setUser(user);
        token1.setTokenHash("token1hash");
        token1.setExpiresAt(ZonedDateTime.now().plusDays(7));
        token1 = entityManager.persistAndFlush(token1);

        token2 = new RefreshToken();
        token2.setUser(user);
        token2.setTokenHash("token2hash");
        token2.setExpiresAt(ZonedDateTime.now().plusDays(7));
        token2 = entityManager.persistAndFlush(token2);
    }

    @Test
    void revokeAllTokensByUserId_ShouldRevokeAllUserTokens() {
        ZonedDateTime revokedAt = ZonedDateTime.now();
        
        int revokedCount = refreshTokenRepository.revokeAllTokensByUserId(user.getId(), revokedAt);
        
        assertEquals(2, revokedCount);
        
        entityManager.refresh(token1);
        entityManager.refresh(token2);
        
        assertNotNull(token1.getRevokedAt());
        assertNotNull(token2.getRevokedAt());
        assertTrue(token1.getRevokedAt().isEqual(revokedAt) || token1.getRevokedAt().isAfter(revokedAt.minusSeconds(1)));
        assertTrue(token2.getRevokedAt().isEqual(revokedAt) || token2.getRevokedAt().isAfter(revokedAt.minusSeconds(1)));
    }

    @Test
    void revokeAllTokensByUserId_AlreadyRevokedTokens_ShouldNotUpdateThem() {
        ZonedDateTime firstRevocation = ZonedDateTime.now().minusHours(1);
        token1.setRevokedAt(firstRevocation);
        entityManager.persistAndFlush(token1);

        ZonedDateTime secondRevocation = ZonedDateTime.now();
        int revokedCount = refreshTokenRepository.revokeAllTokensByUserId(user.getId(), secondRevocation);

        assertEquals(1, revokedCount);
        
        entityManager.refresh(token1);
        entityManager.refresh(token2);
        
        assertEquals(firstRevocation, token1.getRevokedAt());
        assertNotNull(token2.getRevokedAt());
        assertTrue(token2.getRevokedAt().isEqual(secondRevocation) || token2.getRevokedAt().isAfter(secondRevocation.minusSeconds(1)));
    }

    @Test
    void revokeAllTokensByUserId_NonExistentUser_ShouldReturnZero() {
        UUID nonExistentUserId = UUID.randomUUID();
        ZonedDateTime revokedAt = ZonedDateTime.now();
        
        int revokedCount = refreshTokenRepository.revokeAllTokensByUserId(nonExistentUserId, revokedAt);
        
        assertEquals(0, revokedCount);
    }
}