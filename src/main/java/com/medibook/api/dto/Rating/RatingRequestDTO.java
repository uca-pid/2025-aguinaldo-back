package com.medibook.api.dto.Rating;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingRequestDTO {
    private Integer score; 
    private String subcategory; 
}
