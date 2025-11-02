package com.medibook.api.dto.Rating;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;

@Getter
@Setter
@Builder
public class RatingResponseDTO {
    private UUID id;
    private UUID turnId;
    private UUID raterId;
    private UUID ratedId;
    private String raterName;
    private String ratedName;
    private String doctorSpecialty;
    private Integer score;
    private List<String> subcategories;
    private OffsetDateTime createdAt;

    @Deprecated
    public String getSubcategory() {
        if (subcategories == null || subcategories.isEmpty()) return null;
        return subcategories.get(0);
    }
}
