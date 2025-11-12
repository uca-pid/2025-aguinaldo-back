package com.medibook.api.dto.Badge;

import com.medibook.api.entity.PatientBadgeType;
import com.medibook.api.entity.PatientBadgeType.PatientBadgeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientBadgeProgressSummaryDTO {
    private PatientBadgeType badgeType;
    private String badgeName;
    private PatientBadgeCategory category;
    private Boolean earned;
    private Double progressPercentage;
    private String description;
    private String statusMessage;
}