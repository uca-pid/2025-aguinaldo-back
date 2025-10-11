package com.medibook.api.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponseDto {
    private boolean success;
    private String messageId;
    private String message;
    private String errorDetails;
}