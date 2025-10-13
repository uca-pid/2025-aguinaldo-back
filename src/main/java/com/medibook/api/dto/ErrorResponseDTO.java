package com.medibook.api.dto;

import java.time.ZonedDateTime;

import static com.medibook.api.util.DateTimeUtils.ARGENTINA_ZONE;

public record ErrorResponseDTO(
    String error,
    String message,
    int status,
    ZonedDateTime timestamp,
    String path
) {
    public static ErrorResponseDTO of(String error, String message, int status, String path) {
        return new ErrorResponseDTO(error, message, status, ZonedDateTime.now(ARGENTINA_ZONE), path);
    }
}