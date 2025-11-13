package com.medibook.api.dto.Badge;

import com.medibook.api.entity.PatientBadgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientBadgeDTO {
    private PatientBadgeType badgeType;
    private String category;
    private Boolean isActive;
    private OffsetDateTime earnedAt;
    private OffsetDateTime lastEvaluatedAt;
}