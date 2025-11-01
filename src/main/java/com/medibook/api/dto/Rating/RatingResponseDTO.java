package com.medibook.api.dto.Rating;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RatingResponseDTO {
    private UUID id;
    private UUID turnId;
    private UUID raterId;
    private UUID ratedId;
    private Integer score;
    private String subcategory;
    private OffsetDateTime createdAt;
}
