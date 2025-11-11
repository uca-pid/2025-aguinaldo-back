package com.medibook.api.dto.Badge;

import com.medibook.api.entity.BadgeType;
import com.medibook.api.entity.BadgeType.BadgeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeProgressSummaryDTO {
    private BadgeType badgeType;
    private String badgeName;
    private BadgeCategory category;
    private Boolean earned;
    private Double progressPercentage;
    private String description;
    private String statusMessage;
}
