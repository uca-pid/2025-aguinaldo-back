package com.medibook.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.medibook.api.dto.Badge.BadgeProgressSummaryDTO;
import com.medibook.api.entity.Badge;
import com.medibook.api.entity.BadgeType;
import com.medibook.api.entity.BadgeType.BadgeCategory;
import com.medibook.api.entity.BadgeStatistics;
import com.medibook.api.entity.User;
import com.medibook.api.repository.BadgeRepository;
import com.medibook.api.repository.BadgeStatisticsRepository;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeProgressService {

    private final BadgeStatisticsRepository statisticsRepository;
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final BadgeMetadataService badgeMetadataService;

    @Transactional(readOnly = true)
    public List<BadgeProgressSummaryDTO> getBadgeProgress(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("PATIENT".equals(user.getRole())) {
            return getPatientBadgeProgress(user);
        } else if ("DOCTOR".equals(user.getRole())) {
            return getDoctorBadgeProgress(user);
        } else {
            throw new RuntimeException("Unsupported user role: " + user.getRole());
        }
    }

    @Transactional(readOnly = true)
    public List<BadgeProgressSummaryDTO> getDoctorBadgeProgress(User user) {
        UUID userId = user.getId();

        if (!"DOCTOR".equals(user.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

        BadgeStatistics stats = statisticsRepository.findByUserId(userId).orElse(null);
        if (stats == null || stats.getProgress() == null) {
            return getEmptyDoctorProgressList();
        }

        List<Badge> earnedBadges = badgeRepository.findByUser_IdOrderByEarnedAtDesc(userId)
                .stream()
                .filter(Badge::getIsActive)
                .toList();

        List<BadgeProgressSummaryDTO> progressList = new ArrayList<>();
        JsonNode progressJson = stats.getProgress();
        Map<String, com.medibook.api.model.BadgeMetadata> doctorMetadata = badgeMetadataService.getAllDoctorBadgeMetadata();

        for (Map.Entry<String, com.medibook.api.model.BadgeMetadata> entry : doctorMetadata.entrySet()) {
            String badgeType = entry.getKey();
            com.medibook.api.model.BadgeMetadata metadata = entry.getValue();

            Badge badge = earnedBadges.stream()
                    .filter(b -> badgeType.equals(b.getBadgeType()))
                    .findFirst()
                    .orElse(null);

            progressList.add(createProgressDTO(
                    badgeType,
                    progressJson.path(badgeType).asDouble(0.0),
                    badge != null,
                    metadata,
                    badge
            ));
        }

        return progressList;
    }

    @Transactional(readOnly = true)
    public List<BadgeProgressSummaryDTO> getPatientBadgeProgress(User user) {
        UUID userId = user.getId();

        if (!"PATIENT".equals(user.getRole())) {
            throw new RuntimeException("User is not a patient");
        }

        BadgeStatistics stats = statisticsRepository.findByUserId(userId).orElse(null);
        if (stats == null || stats.getProgress() == null) {
            return getEmptyPatientProgressList();
        }

        List<Badge> earnedBadges = badgeRepository.findByUser_IdOrderByEarnedAtDesc(userId)
                .stream()
                .filter(Badge::getIsActive)
                .toList();

        List<BadgeProgressSummaryDTO> progressList = new ArrayList<>();
        JsonNode progressJson = stats.getProgress();
        Map<String, com.medibook.api.model.BadgeMetadata> patientMetadata = badgeMetadataService.getAllPatientBadgeMetadata();

        for (Map.Entry<String, com.medibook.api.model.BadgeMetadata> entry : patientMetadata.entrySet()) {
            String badgeType = entry.getKey();
            com.medibook.api.model.BadgeMetadata metadata = entry.getValue();

            Badge badge = earnedBadges.stream()
                    .filter(b -> badgeType.equals(b.getBadgeType()))
                    .findFirst()
                    .orElse(null);

            progressList.add(createProgressDTO(
                    badgeType,
                    progressJson.path(badgeType).asDouble(0.0),
                    badge != null,
                    metadata,
                    badge
            ));
        }

        return progressList;
    }

    private BadgeProgressSummaryDTO createProgressDTO(
            String badgeType,
            Double progress,
            Boolean earned,
            com.medibook.api.model.BadgeMetadata metadata,
            Badge badge
    ) {
        String statusMessage = earned ?
                "¡Insignia obtenida! Excelente trabajo." :
                (progress >= 75 ? "¡Casi lo logras! Sigue así." :
                        progress >= 50 ? "¡Buen progreso! Estás a mitad de camino." :
                                progress >= 25 ? "¡Vas bien! Continúa esforzándote." :
                                        "Comienza a trabajar en esta insignia.");

        return BadgeProgressSummaryDTO.builder()
                .badgeType(badgeType)
                .badgeName(metadata.getName())
                .category(metadata.getCategory())
                .rarity(metadata.getRarity())
                .description(metadata.getDescription())
                .icon(metadata.getIcon())
                .color(metadata.getColor())
                .criteria(metadata.getCriteria())
                .earned(earned)
                .earnedAt(badge != null ? badge.getEarnedAt() : null)
                .isActive(badge != null ? badge.getIsActive() : null)
                .lastEvaluatedAt(badge != null ? badge.getLastEvaluatedAt() : null)
                .progressPercentage(progress)
                .statusMessage(statusMessage)
                .build();
    }

    private List<BadgeProgressSummaryDTO> getEmptyDoctorProgressList() {
        List<BadgeProgressSummaryDTO> progressList = new ArrayList<>();
        Map<String, com.medibook.api.model.BadgeMetadata> doctorMetadata = badgeMetadataService.getAllDoctorBadgeMetadata();

        for (Map.Entry<String, com.medibook.api.model.BadgeMetadata> entry : doctorMetadata.entrySet()) {
            progressList.add(createProgressDTO(entry.getKey(), 0.0, false, entry.getValue(), null));
        }

        return progressList;
    }

    private List<BadgeProgressSummaryDTO> getEmptyPatientProgressList() {
        List<BadgeProgressSummaryDTO> progressList = new ArrayList<>();
        Map<String, com.medibook.api.model.BadgeMetadata> patientMetadata = badgeMetadataService.getAllPatientBadgeMetadata();

        for (Map.Entry<String, com.medibook.api.model.BadgeMetadata> entry : patientMetadata.entrySet()) {
            progressList.add(createProgressDTO(entry.getKey(), 0.0, false, entry.getValue(), null));
        }

        return progressList;
    }
}
