package com.medibook.api.dto.Registration;

import java.util.UUID;

public record RegisterResponseDTO(
    UUID id,
    String email,
    String name,
    String surname,
    String role
) {}