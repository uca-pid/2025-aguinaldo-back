package com.medibook.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.medibook.api.entity.BadgeStatistics;
import com.medibook.api.repository.BadgeStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class BadgeStatisticsService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BadgeStatisticsRepository statisticsRepository;

    @Async
    @Transactional
    public void updateAfterTurnCompleted(UUID userId, UUID otherUserId) {
        try {
            updateStatistics(userId, stats -> {
                ObjectNode statistics = (ObjectNode) stats;
                int current = statistics.path("total_turns_completed").asInt(0);
                statistics.put("total_turns_completed", current + 1);
            });
        } catch (Exception e) {
            log.error("Error updating turn completion statistics for user {}", userId, e);
        }
    }


    private void updateStatistics(UUID userId, StatisticsUpdater updater) {
        BadgeStatistics stats = getOrCreateStatistics(userId);
        JsonNode statistics = stats.getStatistics();
        updater.update((ObjectNode) statistics);
        stats.setStatistics(objectMapper.valueToTree(statistics));
        statisticsRepository.save(stats);
    }

    private void updateProgress(UUID userId, ProgressUpdater updater) {
        BadgeStatistics stats = getOrCreateStatistics(userId);
        JsonNode progress = stats.getProgress();
        updater.update((ObjectNode) progress);
        stats.setProgress(objectMapper.valueToTree(progress));
        statisticsRepository.save(stats);
    }

    private BadgeStatistics getOrCreateStatistics(UUID userId) {
        return statisticsRepository.findByUserId(userId).orElseGet(() -> {
            try {
                BadgeStatistics stats = BadgeStatistics.builder()
                        .userId(userId)
                        .statistics(objectMapper.readTree("{}"))
                        .progress(objectMapper.readTree("{}"))
                        .build();
                return statisticsRepository.save(stats);
            } catch (JsonProcessingException e) {
                log.error("Error creating statistics for user {}: {}", userId, e.getMessage());
                throw new RuntimeException("Failed to create statistics for user " + userId, e);
            }
        });
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    @FunctionalInterface
    private interface StatisticsUpdater {
        void update(ObjectNode statistics);
    }

    @FunctionalInterface
    private interface ProgressUpdater {
        void update(ObjectNode progress);
    }
}