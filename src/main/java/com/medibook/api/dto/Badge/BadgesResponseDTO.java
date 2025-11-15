package com.medibook.api.dto.Badge;

import com.medibook.api.entity.BadgeType.BadgeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BadgesResponseDTO {
    private UUID userId;
    private String userName;
    private String role;
    private int totalActiveBadges;
    private Map<BadgeCategory, java.util.List<BadgeDTO>> badgesByCategory;
}