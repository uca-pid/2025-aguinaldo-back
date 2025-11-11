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
public class DoctorBadgesResponseDTO {
    private UUID doctorId;
    private String doctorName;
    private Integer totalActiveBadges;
    private List<BadgeDTO> qualityOfCareBadges;
    private List<BadgeDTO> professionalismBadges;
    private List<BadgeDTO> consistencyBadges;
}
