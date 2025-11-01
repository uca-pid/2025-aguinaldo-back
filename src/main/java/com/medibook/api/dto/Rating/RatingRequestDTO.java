package com.medibook.api.dto.Rating;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonAlias;

@Getter
@Setter
public class RatingRequestDTO {
    private Integer score;
    
    @JsonAlias({"subcategory", "subcategories"})
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> subcategories;
}
