package com.medibook.api.dto.Badge;

import com.medibook.api.entity.BadgeType.BadgeCategory;
import com.medibook.api.model.BadgeMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeProgressSummaryDTO {
    private String badgeType;
    private String badgeName;
    private BadgeCategory category;
    private BadgeMetadata.BadgeRarity rarity;
    private String description;
    private String icon;
    private String color;
    private String criteria;
    private Boolean earned;
    private OffsetDateTime earnedAt;
    private Boolean isActive;
    private OffsetDateTime lastEvaluatedAt;
    private Double progressPercentage;
    private String statusMessage;
}
