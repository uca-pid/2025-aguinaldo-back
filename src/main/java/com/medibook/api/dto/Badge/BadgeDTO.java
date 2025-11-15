package com.medibook.api.dto.Badge;

import com.medibook.api.entity.BadgeType.BadgeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BadgeDTO {
    private UUID id;
    private String badgeType;
    private BadgeCategory category;
    private OffsetDateTime earnedAt;
    private Boolean isActive;
    private OffsetDateTime lastEvaluatedAt;
}