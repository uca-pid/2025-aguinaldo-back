package com.medibook.api.dto.Rating;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubcategoryCountDTO {
    private String subcategory;
    private Long count;
}
