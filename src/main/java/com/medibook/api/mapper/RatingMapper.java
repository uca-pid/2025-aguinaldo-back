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

    String doctorSpecialty = null;
    if ("DOCTOR".equals(r.getRated().getRole()) && r.getRated().getDoctorProfile() != null) {
        doctorSpecialty = r.getRated().getDoctorProfile().getSpecialty();
    } else if ("DOCTOR".equals(r.getRater().getRole()) && r.getRater().getDoctorProfile() != null) {
        doctorSpecialty = r.getRater().getDoctorProfile().getSpecialty();
    }

    return RatingResponseDTO.builder()
                .id(r.getId())
                .turnId(r.getTurnAssigned().getId())
                .raterId(r.getRater().getId())
                .ratedId(r.getRated().getId())
                .raterName(r.getRater().getName() + " " + r.getRater().getSurname())
                .ratedName(r.getRated().getName() + " " + r.getRated().getSurname())
                .doctorSpecialty(doctorSpecialty)
                .score(r.getScore())
        .subcategories(subcats)
                .createdAt(r.getCreatedAt())
                .build();
    }
}
