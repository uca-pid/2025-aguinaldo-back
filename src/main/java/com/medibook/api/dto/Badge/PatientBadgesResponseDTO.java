package com.medibook.api.dto.Badge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientBadgesResponseDTO {
    private UUID patientId;
    private String patientName;
    private Integer totalActiveBadges;
    private List<PatientBadgeDTO> healthCommitmentBadges;
    private List<PatientBadgeDTO> responsibilityBadges;
    private List<PatientBadgeDTO> preparationBadges;
}