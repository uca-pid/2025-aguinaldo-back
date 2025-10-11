package com.medibook.api.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDto {
    private String to;
    private String toName;
    private String subject;
    private String htmlContent;
    private String textContent;
    private String templateId;
    private Object templateVariables;
}