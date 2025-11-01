package com.medibook.api.mapper;

import com.medibook.api.dto.Rating.RatingResponseDTO;
import com.medibook.api.entity.Rating;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RatingMapper {

    public RatingResponseDTO toDTO(Rating r) {
    List<String> subcats = r.getSubcategory() == null ? List.of() : Arrays.stream(r.getSubcategory().split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());

    return RatingResponseDTO.builder()
                .id(r.getId())
                .turnId(r.getTurnAssigned().getId())
                .raterId(r.getRater().getId())
                .ratedId(r.getRated().getId())
                .score(r.getScore())
        .subcategories(subcats)
                .createdAt(r.getCreatedAt())
                .build();
    }
}
