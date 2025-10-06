package com.medibook.api.dto.Auth;

import jakarta.validation.constraints.*;

public record SignInRequestDTO(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 254, message = "Email must be less than 254 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(max = 128, message = "Password must be less than 128 characters")
    String password
) {}