package com.medibook.api.mapper;

import com.medibook.api.dto.Rating.RatingResponseDTO;
import com.medibook.api.entity.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public RatingResponseDTO toDTO(Rating r) {
        return RatingResponseDTO.builder()
                .id(r.getId())
                .turnId(r.getTurnAssigned().getId())
                .raterId(r.getRater().getId())
                .ratedId(r.getRated().getId())
                .score(r.getScore())
                .subcategory(r.getSubcategory())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
