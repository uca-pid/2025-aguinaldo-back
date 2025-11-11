package com.medibook.api.dto.Badge;

import com.medibook.api.entity.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDTO {
    private BadgeType badgeType;
    private String category;
    private Boolean isActive;
    private OffsetDateTime earnedAt;
    private OffsetDateTime lastEvaluatedAt;
}
