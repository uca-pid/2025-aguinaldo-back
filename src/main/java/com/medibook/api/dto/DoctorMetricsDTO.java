package com.medibook.api.dto;

import com.medibook.api.dto.Rating.SubcategoryCountDTO;
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
public class DoctorMetricsDTO {
    private UUID doctorId;
    private String name;
    private String surname;
    private String specialty;
    private Double score;
    private List<SubcategoryCountDTO> ratingSubcategories;
    private Integer totalPatients;
    private Integer upcomingTurns;
    private Integer completedTurnsThisMonth;
    private Integer cancelledTurns;
}
