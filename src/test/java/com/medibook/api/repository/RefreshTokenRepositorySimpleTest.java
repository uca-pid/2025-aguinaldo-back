package com.medibook.api.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import com.medibook.api.entity.RefreshToken;
import com.medibook.api.entity.User;

import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.liquibase.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RefreshTokenRepositorySimpleTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setDni(12345678L);
        testUser.setPasswordHash("hashedpassword");
        testUser.setName("Test");
        testUser.setSurname("User");
        testUser.setPhone("123456789");
        testUser.setBirthdate(LocalDate.of(1990, 1, 1));
        testUser.setGender("M");
        testUser.setEmailVerified(true);
        testUser.setStatus("ACTIVE");
        testUser.setRole("PATIENT");
        
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void revokeAllTokensByUserId_ShouldRevokeAllUserTokens() {
        ZonedDateTime now = ZonedDateTime.now();
        RefreshToken token1 = createRefreshToken(testUser, "token1", null);
        RefreshToken token2 = createRefreshToken(testUser, "token2", null);
        RefreshToken token3 = createRefreshToken(testUser, "token3", now.minusHours(1));
        
        entityManager.persistAndFlush(token1);
        entityManager.persistAndFlush(token2);
        entityManager.persistAndFlush(token3);
        entityManager.clear();

        int revokedCount = refreshTokenRepository.revokeAllTokensByUserId(testUser.getId(), now);

        assertThat(revokedCount).isEqualTo(2);
        
        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        long revokedTokens = tokens.stream()
            .filter(RefreshToken::isRevoked)
            .count();
        
        assertThat(revokedTokens).isEqualTo(3);
        
        System.out.println("Tokens revocados exitosamente: " + revokedCount);
    }

    @Test
    void revokeAllTokensByUserId_NonExistentUser_ShouldReturnZero() {
        UUID nonExistentUserId = UUID.randomUUID();
        ZonedDateTime now = ZonedDateTime.now();

        int revokedCount = refreshTokenRepository.revokeAllTokensByUserId(nonExistentUserId, now);

        assertThat(revokedCount).isEqualTo(0);
        
        System.out.println("No se encontraron tokens para el usuario inexistente");
    }

    @Test
    void revokeAllTokensByUserId_AlreadyRevokedTokens_ShouldNotUpdateThem() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime pastTime = now.minusHours(1);
        RefreshToken token1 = createRefreshToken(testUser, "token1", pastTime);
        RefreshToken token2 = createRefreshToken(testUser, "token2", pastTime);
        
        entityManager.persistAndFlush(token1);
        entityManager.persistAndFlush(token2);
        entityManager.clear();

        int revokedCount = refreshTokenRepository.revokeAllTokensByUserId(testUser.getId(), now);

        assertThat(revokedCount).isEqualTo(0);
        
        System.out.println("No se revocaron tokens ya revocados: " + revokedCount);
    }

    private RefreshToken createRefreshToken(User user, String tokenHash, ZonedDateTime revokedAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(ZonedDateTime.now().plusDays(7));
        refreshToken.setRevokedAt(revokedAt);
        refreshToken.setCreatedAt(ZonedDateTime.now());
        refreshToken.setUserAgent("Test Agent");
        refreshToken.setIpAddress("127.0.0.1");
        return refreshToken;
    }
}