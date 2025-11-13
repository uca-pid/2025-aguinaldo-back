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
    private List<PatientBadgeDTO> welcomeBadges;
    private List<PatientBadgeDTO> preventiveCareBadges;
    private List<PatientBadgeDTO> activeCommitmentBadges;
    private List<PatientBadgeDTO> clinicalExcellenceBadges;
}