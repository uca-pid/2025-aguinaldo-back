package com.medibook.api.model;

import com.medibook.api.entity.BadgeType.BadgeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeMetadata {
    private String badgeType;
    private BadgeCategory category;
    private BadgeRarity rarity;
    private String name;
    private String description;
    private String icon;
    private String color;
    private String criteria;

    public enum BadgeRarity {
        COMMON, RARE, EPIC, LEGENDARY
    }
}