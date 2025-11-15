package com.medibook.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.api.entity.BadgeStatistics;
import com.medibook.api.entity.User;
import com.medibook.api.repository.BadgeStatisticsRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableAsync
class BadgeStatisticsServiceTest {

    @MockitoBean
    private BadgeStatisticsRepository statisticsRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private BadgeStatisticsService badgeStatisticsService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setRole("PATIENT");
    }

    @Test
    void updateAfterTurnCompleted_ExistingStats_UpdatesStatistics() {
        BadgeStatistics existingStats = BadgeStatistics.builder()
                .userId(userId)
                .statistics(objectMapper.createObjectNode())
                .progress(objectMapper.createObjectNode())
                .build();

        when(statisticsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(statisticsRepository.save(any(BadgeStatistics.class))).thenReturn(existingStats);

        badgeStatisticsService.updateAfterTurnCompleted(userId, UUID.randomUUID());

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(statisticsRepository).findByUserId(userId);
            verify(statisticsRepository).save(any(BadgeStatistics.class));
        });
    }

    @Test
    void updateAfterTurnCompleted_NoExistingStats_CreatesNewStats() {
        when(statisticsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        BadgeStatistics newStats = BadgeStatistics.builder()
                .userId(userId)
                .statistics(objectMapper.createObjectNode())
                .progress(objectMapper.createObjectNode())
                .build();

        when(statisticsRepository.save(any(BadgeStatistics.class))).thenReturn(newStats);

        badgeStatisticsService.updateAfterTurnCompleted(userId, UUID.randomUUID());

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(statisticsRepository).findByUserId(userId);
            verify(statisticsRepository, times(2)).save(any(BadgeStatistics.class));
        });
    }

    @Test
    void updateAfterTurnCompleted_ExceptionInSave_LogsError() {
        BadgeStatistics existingStats = BadgeStatistics.builder()
                .userId(userId)
                .statistics(objectMapper.createObjectNode())
                .progress(objectMapper.createObjectNode())
                .build();

        when(statisticsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(statisticsRepository.save(any(BadgeStatistics.class))).thenThrow(new RuntimeException("Database error"));

        badgeStatisticsService.updateAfterTurnCompleted(userId, UUID.randomUUID());

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(statisticsRepository).findByUserId(userId);
            verify(statisticsRepository).save(any(BadgeStatistics.class));
        });
    }
}