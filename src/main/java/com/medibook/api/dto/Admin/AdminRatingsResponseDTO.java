package com.medibook.api.dto.Admin;

import com.medibook.api.dto.Rating.RatingResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AdminRatingsResponseDTO {
    private List<RatingResponseDTO> allRatings;
    private List<RatingResponseDTO> patientRatings; 
    private List<RatingResponseDTO> doctorRatings;  
    private RatingStatsDTO stats;
    
    @Getter
    @Setter
    @Builder
    public static class RatingStatsDTO {
        private long totalRatings;
        private long patientRatingsCount;
        private long doctorRatingsCount;  
        private double averageScore;
        private double averagePatientRating;
        private double averageDoctorRating;
    }
}