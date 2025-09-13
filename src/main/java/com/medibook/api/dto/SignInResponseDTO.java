package com.medibook.api.dto;

import java.util.UUID;

public record SignInResponseDTO(
    UUID id,
    String email,
    String name,
    String surname,
    String role,
    String accessToken,
    String refreshToken
) {}