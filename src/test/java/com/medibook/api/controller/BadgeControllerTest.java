package com.medibook.api.controller;

import com.medibook.api.dto.Badge.BadgeProgressSummaryDTO;
import com.medibook.api.dto.Badge.BadgesResponseDTO;
import com.medibook.api.dto.Badge.BadgeDTO;
import com.medibook.api.entity.BadgeType.BadgeCategory;
import com.medibook.api.entity.User;
import com.medibook.api.service.BadgeService;
import com.medibook.api.service.BadgeStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeControllerTest {

    @Mock
    private BadgeService badgeService;

    @Mock
    private BadgeStatisticsService badgeStatisticsService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BadgeController badgeController;

    private UUID userId;
    private User user;
    private BadgesResponseDTO badgesResponse;
    private List<BadgeProgressSummaryDTO> progressList;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("Test");
        user.setSurname("User");
        user.setRole("PATIENT");

        badgesResponse = BadgesResponseDTO.builder()
                .userId(userId)
                .userName("Test User")
                .role("PATIENT")
                .totalActiveBadges(2)
                .badgesByCategory(Map.of(
                        BadgeCategory.WELCOME, List.of(
                                BadgeDTO.builder()
                                        .id(UUID.randomUUID())
                                        .badgeType("PATIENT_MEDIBOOK_WELCOME")
                                        .category(BadgeCategory.WELCOME)
                                        .earnedAt(OffsetDateTime.now())
                                        .isActive(true)
                                        .lastEvaluatedAt(OffsetDateTime.now())
                                        .build()
                        )
                ))
                .build();

        progressList = List.of(
                BadgeProgressSummaryDTO.builder()
                        .badgeType("PATIENT_MEDIBOOK_WELCOME")
                        .badgeName("Bienvenido a MediBook")
                        .category(BadgeCategory.WELCOME)
                        .rarity(com.medibook.api.model.BadgeMetadata.BadgeRarity.COMMON)
                        .description("Primer paso en MediBook")
                        .icon("welcome")
                        .color("#00FF00")
                        .criteria("Completar 1 turno")
                        .earned(true)
                        .progressPercentage(100.0)
                        .statusMessage("¡Insignia obtenida!")
                        .build()
        );
    }

    @Test
    void getUserBadges_ReturnsBadgesResponse() {
        when(badgeService.getUserBadges(userId)).thenReturn(badgesResponse);

        ResponseEntity<BadgesResponseDTO> response = badgeController.getUserBadges(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        assertEquals("Test User", response.getBody().getUserName());
        assertEquals(2, response.getBody().getTotalActiveBadges());

        verify(badgeService).getUserBadges(userId);
    }

    @Test
    void getMyBadges_ReturnsBadgesResponse() {
        when(authentication.getPrincipal()).thenReturn(user);
        when(badgeService.getUserBadges(userId)).thenReturn(badgesResponse);

        ResponseEntity<BadgesResponseDTO> response = badgeController.getMyBadges(authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());

        verify(authentication).getPrincipal();
        verify(badgeService).getUserBadges(userId);
    }

    @Test
    void getUserBadgeProgress_ReturnsProgressList() {
        when(badgeService.getUserBadgeProgress(userId)).thenReturn(progressList);

        ResponseEntity<List<BadgeProgressSummaryDTO>> response = badgeController.getUserBadgeProgress(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals("PATIENT_MEDIBOOK_WELCOME", response.getBody().get(0).getBadgeType());

        verify(badgeService).getUserBadgeProgress(userId);
    }

    @Test
    void getMyBadgeProgress_ReturnsProgressList() {
        when(authentication.getPrincipal()).thenReturn(user);
        when(badgeService.getUserBadgeProgress(userId)).thenReturn(progressList);

        ResponseEntity<List<BadgeProgressSummaryDTO>> response = badgeController.getMyBadgeProgress(authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());

        verify(authentication).getPrincipal();
        verify(badgeService).getUserBadgeProgress(userId);
    }

    @Test
    void evaluateUserBadges_CallsServiceAndReturnsOk() {
        doNothing().when(badgeService).evaluateAllBadges(userId);

        ResponseEntity<Void> response = badgeController.evaluateUserBadges(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());

        verify(badgeService).evaluateAllBadges(userId);
    }

    @Test
    void evaluateMyBadges_CallsServiceAndReturnsOk() {
        when(authentication.getPrincipal()).thenReturn(user);
        doNothing().when(badgeService).evaluateAllBadges(userId);

        ResponseEntity<Void> response = badgeController.evaluateMyBadges(authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());

        verify(authentication).getPrincipal();
        verify(badgeService).evaluateAllBadges(userId);
    }
}